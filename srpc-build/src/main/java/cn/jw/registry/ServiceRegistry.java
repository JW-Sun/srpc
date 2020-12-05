package cn.jw.registry;

import java.net.InetSocketAddress;

public interface ServiceRegistry {

    void serviceRegistry(String rpcServiceName, InetSocketAddress inetSocketAddress);

}
