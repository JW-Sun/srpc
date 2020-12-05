package cn.jw.remoting.transport.netty_01_attributeMap.codec;

import cn.jw.serialize.MySerializer;
import com.esotericsoftware.kryo.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.units.qual.A;

/**
 *  网络传输需要通流来进行实现，ByteBuf是Netty提供的字节数据的容器，使更容易处理字节数据
 */
@AllArgsConstructor
public class NettyKryoEncoder extends MessageToByteEncoder<Object> {

    private final MySerializer serializer;
    private final Class<?> genericClass;

    /**
     *  将对象转换成字节码后放到ByteBuf的对象中
     * @param ctx
     * @param msg
     * @param out
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (genericClass.isInstance(msg)) {
            // 1. 对象转换成byte
            byte[] body = serializer.serialize(msg);

            // 2. 消息的长度
            int len = body.length;

            // 3. 写入消息对应的字节数组的长度，
            out.writeInt(len);

            // 4. 将字节数组 写入ByteBuf中
            out.writeBytes(body);

        }
    }
}
