package cn.jw.remoting.transport.netty.client;

import cn.jw.enums.CompressTypeEnum;
import cn.jw.enums.SerializationTypeEnum;
import cn.jw.remoting.constants.RpcConstants;
import cn.jw.remoting.dto.RpcMessage;
import cn.jw.remoting.dto.RpcResponse;
import com.sun.org.apache.xpath.internal.objects.XObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 *  获得从服务端返回的数据并使用优化过后的completableFuture来进行读取.
 *
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
    private final NettyRpcClient nettyRpcClient;
    private final UnprocessedRequests unprocessedRequests;

    public NettyRpcClientHandler() {
        this.nettyRpcClient = new NettyRpcClient();
        this.unprocessedRequests = new UnprocessedRequests();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("客户端handler接收消息：[{}]", msg);
        try {
            if (msg instanceof RpcMessage) {
                RpcMessage message = (RpcMessage) msg;
                byte messageType = message.getMessageType();
                if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                    log.info("客户端handler的心跳监测，[{}]", message);
                } else if (messageType == RpcConstants.RESPONSE_TYPE) {
                    Object data = message.getData();
                    RpcResponse<Object> response = (RpcResponse<Object>) data;
                    // 里面调用completableFuture.complete方法来计算
                    unprocessedRequests.complete(response);
                }
            }
        } catch (Exception e) {
            log.error("客户端处理返回消息的");
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
                rpcMessage.setData(RpcConstants.PING);
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("捕获到了异常：{}", cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}
