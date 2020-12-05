package cn.jw.remoting.transport.netty_01_attributeMap;

import cn.jw.remoting.dto.RpcRequest;
import cn.jw.remoting.dto.RpcResponse;
import cn.jw.remoting.transport.netty_01_attributeMap.codec.NettyKryoDecoder;
import cn.jw.remoting.transport.netty_01_attributeMap.codec.NettyKryoEncoder;
import cn.jw.serialize.KryoSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);
    private final String host;
    private final int port;
    private static final Bootstrap b;

    // 初始化相关资源 EventLoopGroup Bootstrap
    static {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        b = new Bootstrap();
        KryoSerializer kryoSerializer = new KryoSerializer();

        b.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                // 连接超时的时间，超过这个时间就是就是建立失败
                // 15苗没有发送数据给服务端，会发送一次心跳请求
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        // 自定义序列化编解码器
                        // RpcResponse -> ByteBuf
                        socketChannel.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcResponse.class));

                        // BYteBuf -> RpcRequest
                        socketChannel.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcRequest.class));
                        socketChannel.pipeline().addLast(new NettyClientHandler());
                    }
                });
    }


    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     *  发送消息到服务端
     *
     *  1. 初始化Bootstrap
     *  2. 通过Bootstrap连接服务器
     *  3. 通过Channel向服务器发送消息RpcRequest
     *  4. 发送成功后，阻塞等待，直到Channel关闭
     *  5. 拿到服务端返回的结果RpcResponse
     *
     * @param rpcRequest 消息体
     * @return 服务端返回的数据
     */
    public RpcResponse sendMessage(RpcRequest rpcRequest) {
        try {
            ChannelFuture f = b.connect(host, port).sync();
            logger.info("client connect {}", host + ":" + port);
            /**
             *  channel实现了AttributeMap的接口，也就表明存在了AttributeMap的想爱你噶管属性，每个channel上的AttributeMap属于共享数据
             */
            Channel futureChannel = f.channel();
            logger.info("send message");
            if (futureChannel != null) {
                futureChannel.writeAndFlush(rpcRequest)
                        .addListener(new GenericFutureListener<Future<? super Void>>() {
                            @Override
                            public void operationComplete(Future<? super Void> future) throws Exception {
                                if (future.isSuccess()) {
                                    logger.info("client send message: [{}]", rpcRequest.toString());
                                } else {
                                    logger.error("send fail:", future.cause());
                                }
                            }
                        });

                // 阻塞等待，直到Channel关闭（这里可以进行优化，避免一直堵塞）
                futureChannel.closeFuture().sync();

                // 将服务端的数据返回的数据，取出RpcResponse对象
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
                return futureChannel.attr(key).get();
            }
        } catch (InterruptedException e) {
            logger.error("occur exception when connect server:", e);
        }
        return null;
    }
}
