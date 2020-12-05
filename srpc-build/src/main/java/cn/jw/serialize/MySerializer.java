package cn.jw.serialize;

import cn.jw.extension.SPI;

@SPI
public interface MySerializer {

    byte[] serialize(Object o);

    <T> T deserialize(byte[] bytes, Class<T> clazz);

}
