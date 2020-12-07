package cn.jw.registry;

import cn.jw.extension.SPI;

import java.net.InetSocketAddress;

@SPI
public interface ServiceDiscover {

    /**
     * 通过服务的名称来查找服务
     * @param rpcServiceName
     * @return
     */
    InetSocketAddress lookupService(String rpcServiceName);
}
