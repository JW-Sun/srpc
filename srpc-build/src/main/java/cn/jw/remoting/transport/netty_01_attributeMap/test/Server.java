package cn.jw.remoting.transport.netty_01_attributeMap.test;

import cn.jw.remoting.transport.netty_01_attributeMap.NettyClient;
import cn.jw.remoting.transport.netty_01_attributeMap.NettyServer;

public class Server {
    public static void main(String[] args) {
        new NettyServer(8889).run();
    }
}
