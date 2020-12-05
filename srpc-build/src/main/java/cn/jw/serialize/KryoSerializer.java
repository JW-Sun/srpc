package cn.jw.serialize;

import cn.jw.remoting.dto.RpcRequest;
import cn.jw.remoting.dto.RpcResponse;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.sun.xml.internal.ws.encoding.soap.SerializationException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

/**
 *  Kryo序列化的性能比较强大
 */
@Slf4j
public class KryoSerializer implements MySerializer {

    // Kyro线程不安全，需要在不同的线程中做线程隔离
    private static final ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.register(RpcRequest.class);
            kryo.register(RpcResponse.class);
            return kryo;
        }
    };


    /**
     *  将对象序列化成字节数组
     * @param o
     * @return
     */
    @Override
    public byte[] serialize(Object o) {
        try (Output output = new Output(new ByteArrayOutputStream())) {
            // 每个线程专有的kryo对象
            Kryo kryo = kryoThreadLocal.get();
            kryo.writeObject(output, o);
            // 为什么要remove?
            kryoThreadLocal.remove();
            return output.toBytes();
        } catch (Exception e) {
            throw new SerializationException("kryo serialize failed");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (Input input = new Input(new ByteArrayInputStream(bytes))) {
            Kryo kryo = kryoThreadLocal.get();
            Object obj = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();
            return clazz.cast(obj);
        } catch (Exception e) {
            throw new SerializationException("kryo deserialize failed");
        }
    }
}
