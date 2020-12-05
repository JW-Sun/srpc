package cn.jw.remoting.transport.netty.client;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *   保存Channel的对象
 */
public class ChannelProvider {
    private final Map<String, Channel> map;

    public ChannelProvider() {
        this.map = new ConcurrentHashMap<>();
    }

    public Channel get(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        if (map.containsKey(key)) {
            Channel channel = map.get(key);
            if (channel != null && channel.isActive()) {
                return channel;
            }
            map.remove(key);
        }
        return null;
    }

    public void set(InetSocketAddress inetSocketAddress, Channel channel) {
        String key = inetSocketAddress.toString();
        map.put(key, channel);
    }
}
