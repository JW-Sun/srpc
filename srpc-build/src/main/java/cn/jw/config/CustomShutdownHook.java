package cn.jw.config;

import cn.jw.registry.zk.util.CuratorUtil;
import cn.jw.remoting.transport.netty.server.NettyRpcServer;
import cn.jw.utils.concurrent.threadpool.ThreadPoolFactory;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

@Slf4j
public class CustomShutdownHook {

    // 可以使用静态内部类的方式进行获得对应的对象。

    private static CustomShutdownHook customShutdownHook;

    private CustomShutdownHook() {}

    private static class Singleton {
        public static CustomShutdownHook singleton = new CustomShutdownHook();
    }

    public static CustomShutdownHook getCustomShutdownHook() {
        return Singleton.singleton;
    }

    public void clearAll() {
        log.info("关闭钩子清qingchu");
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), NettyRpcServer.PORT);
                    CuratorUtil.clearRegistry(CuratorUtil.getZkClient(), inetSocketAddress);
                } catch (UnknownHostException e) {

                }
                ThreadPoolFactory.shutdownAllThreadPool();
            }
        }));
    }

}
