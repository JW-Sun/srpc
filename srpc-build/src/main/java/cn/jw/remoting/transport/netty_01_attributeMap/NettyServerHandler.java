package cn.jw.remoting.transport.netty_01_attributeMap;

import cn.jw.remoting.dto.RpcRequest;
import cn.jw.remoting.dto.RpcResponse;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *  用于处理客户端发送过来的消息，并且返回给客户端
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServerHandler.class);
    private static final AtomicInteger atomicInteger = new AtomicInteger(1);

    // 服务端处理发送过来的消息
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            RpcRequest rpcRequest = (RpcRequest) msg;
            LOGGER.info("server receive msg: [{}], times: [{}]", rpcRequest, atomicInteger.getAndIncrement());

            RpcResponse msgFromServer = RpcResponse.builder().message("message from server").build();

            ChannelFuture f = ctx.writeAndFlush(msgFromServer);
            f.addListener(ChannelFutureListener.CLOSE);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("server catch exception: ", cause);
        ctx.close();
    }
}
