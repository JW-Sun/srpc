package cn.jw.remoting.transport.netty.server;

import cn.jw.config.CustomShutdownHook;
import cn.jw.entity.RpcServiceProperties;
import cn.jw.factory.SingletonFactory;
import cn.jw.provider.ServiceProvider;
import cn.jw.remoting.transport.netty.codec.RpcMessageDecoder;
import cn.jw.remoting.transport.netty.codec.RpcMessageEncoder;
import cn.jw.utils.RuntimeUtil;
import cn.jw.utils.concurrent.threadpool.ThreadPoolFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jboss.netty.handler.codec.spdy.SpdyOrHttpChooser;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class NettyRpcServer {
    public static final int PORT = 9988;

    private final ServiceProvider serviceProvider = SingletonFactory.getSingleton(ServiceProvider.class);

    public NettyRpcServer() {

    }

    public void registerService(Object service, RpcServiceProperties rpcServiceProperties) {
        serviceProvider.publishService(service, rpcServiceProperties);
    }

    @SneakyThrows
    public void start() {
        CustomShutdownHook.getCustomShutdownHook().clearAll();
        String host = InetAddress.getLocalHost().getHostAddress();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
                RuntimeUtil.cpus(),
                ThreadPoolFactory.buildThreadFactory("service-handler-group", false)
        );

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY, true) // Nagle算法，尽可能发送大数据块，减少网络传输
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // 是否开启Tcp底层心跳机制
                    .option(ChannelOption.SO_BACKLOG, 128) // 系统用于临时存放已经完成三次握手的请求队列的最大长度，可以调优
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 30s没有收到客户端请求就关闭连接
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            p.addLast(new RpcMessageEncoder());
                            p.addLast(new RpcMessageDecoder());
                            p.addLast(serviceHandlerGroup, new NettyRpcServerHandler());
                        }
                    });

            // 绑定端口，同步等待绑定成功
            ChannelFuture channelFuture = serverBootstrap.bind(host, PORT).sync();
            // 等待服务端监听端口关闭
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("服务端启动出错：{}", e.getMessage());
        } finally {
            log.info("服务端关闭两个group");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();
        }

    }

}
