package cn.jw.remoting.transport.netty.client;

import cn.jw.enums.CompressTypeEnum;
import cn.jw.enums.SerializationTypeEnum;
import cn.jw.extension.ExtensionLoader;
import cn.jw.factory.SingletonFactory;
import cn.jw.registry.ServiceDiscover;
import cn.jw.remoting.constants.RpcConstants;
import cn.jw.remoting.dto.RpcMessage;
import cn.jw.remoting.dto.RpcRequest;
import cn.jw.remoting.dto.RpcResponse;
import cn.jw.remoting.transport.RpcRequestTransport;
import cn.jw.remoting.transport.netty.codec.RpcMessageDecoder;
import cn.jw.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 *   初始化以及关闭 BootStrap object
 */
@Slf4j
public final class NettyRpcClient implements RpcRequestTransport {

    // 服务发现
    private final ServiceDiscover serviceDiscover;

    // 服务端没有处理的
    private final UnprocessedRequests unprocessedRequests;

    // Channel提供管理
    private final ChannelProvider channelProvider;

    private final Bootstrap bootstrap;

    private final EventLoopGroup eventLoopGroup;

    public NettyRpcClient() {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                // 连接的超时时间
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        // 多长时间没有数据传输，发送心跳检测
                        p.addLast(new IdleStateHandler(0, 15, 0, TimeUnit.SECONDS));
                        p.addLast(new RpcMessageEncoder());
                        p.addLast(new RpcMessageDecoder());
                        p.addLast(new NettyRpcClientHandler());
                    }
                });

        this.serviceDiscover = ExtensionLoader.getExtensionLoader(ServiceDiscover.class).getExtension("zk");
        this.unprocessedRequests = SingletonFactory.getSingleton(UnprocessedRequests.class);
        this.channelProvider = SingletonFactory.getSingleton(ChannelProvider.class);
    }

    /**
     * 连接上server并且获得channel，然后可以发送rpc的消息给服务端
     * @param inetSocketAddress
     * @return
     */
    public Channel connect(InetSocketAddress inetSocketAddress) throws ExecutionException, InterruptedException {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress)
                // 监听channel的构造成功情况
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            log.info("客户端连接服务器[{}]成功！", inetSocketAddress.toString());
                            completableFuture.complete(future.channel());
                        } else {
                            throw new IllegalStateException();
                        }
                    }
                });
        return completableFuture.get();
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) throws ExecutionException, InterruptedException {
        CompletableFuture<RpcResponse<Object>> resFuture = new CompletableFuture<>();
        String rpcServiceName = rpcRequest.toRpcProperties().toRpcServiceName();
        InetSocketAddress inetSocketAddress = serviceDiscover.lookupService(rpcServiceName);

        // 获得channel
        Channel channel = getChannel(inetSocketAddress);
        if (channel.isActive()) {
            // 存入未处理请求
            unprocessedRequests.put(rpcRequest.getRequestId(), resFuture);
            RpcMessage rpcMessage = new RpcMessage();
            rpcMessage.setData(rpcRequest);
            rpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
            rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
            rpcMessage.setMessageType(RpcConstants.REQUEST_TYPE);

            // 向channel写数据并落盘
            channel.writeAndFlush(rpcMessage).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        log.info("客户端成功发送消息：[{}]", rpcMessage);
                    } else {
                        future.channel().close();
                        resFuture.completeExceptionally(future.cause());
                        log.error("客户端发送消息失败：{}", future.cause());
                    }
                }
            });
        } else {
            throw new IllegalStateException();
        }
        return resFuture;
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress) throws ExecutionException, InterruptedException {
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel == null) {
            channel = connect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }

    public void shutdown() {
        eventLoopGroup.shutdownGracefully();
    }
}
