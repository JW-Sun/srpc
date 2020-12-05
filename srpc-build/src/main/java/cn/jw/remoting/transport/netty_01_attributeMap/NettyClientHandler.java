package cn.jw.remoting.transport.netty_01_attributeMap;

import cn.jw.remoting.dto.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.apache.log4j.lf5.viewer.LogFactor5Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  自定义ChannelHandler处理服务端的消息
 *  用于读取服务端发送过来的RpcResponse消息对象，并将RpcResponse消息保存到AttributeMap上，看作一个Channel的共享数据源
 */
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyClientHandler.class);

    /**
     *  channel读取服务端过来的消息
     * @param ctx
     * @param msg 消息
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            RpcResponse response = (RpcResponse) msg;
            LOGGER.info("client receive msg: [{}]", response.toString());

            // 声明的AttributeKey对象
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
            // 将服务端的返回结果保存到AttributeMap上，AttributeMap可以看做是一个Channel的共享数据源
            ctx.channel().attr(key).set(response);
            ctx.channel().close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("client caught exception, ", cause);
        ctx.close();
    }
}
