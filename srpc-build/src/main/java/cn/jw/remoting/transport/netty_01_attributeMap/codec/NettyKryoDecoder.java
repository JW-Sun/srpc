package cn.jw.remoting.transport.netty_01_attributeMap.codec;

import cn.jw.serialize.MySerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@AllArgsConstructor
public class NettyKryoDecoder extends ByteToMessageDecoder {

    private MySerializer serializer;

    private Class<?> genericClass;

    private static final int BODY_LENGTH = 4;

    /**
     *  解码ByteBuf对象
     * @param ctx
     * @param in 需要解码的ByteBuf对象
     * @param out 解码之后的数据对象需要添加到out对象
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 1. byteBuf中的消息长度所占的子树已经是4，所以byteBuf的可读字节必须要大于4
        if (in.readableBytes() >= BODY_LENGTH) {
            // 2. 标记当前readIndex的位置，以便后面的重置的readIndex的时候使用
            in.markReaderIndex();

            // 3. 消息的长度
            int dataLen = in.readInt();
            if (dataLen < 0 || in.readableBytes() < 0) {
                log.error("data length or ByteBuf readableBytes is not valid");
                return;
            }
            // 4. 处理不完整信息
            if (in.readableBytes() < dataLen) {
                in.resetReaderIndex();
                return;
            }

            byte[] body = new byte[dataLen];
            in.readBytes(body);
            Object object = serializer.deserialize(body, genericClass);
            out.add(object);
            log.info("successfully decode ByteBuf to Object");
        }
    }
}
