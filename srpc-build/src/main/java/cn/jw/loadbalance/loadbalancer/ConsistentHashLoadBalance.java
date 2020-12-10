package cn.jw.loadbalance.loadbalancer;

import cn.jw.loadbalance.AbstractLoadBalance;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ConsistentHashLoadBalance extends AbstractLoadBalance {

    private final ConcurrentHashMap<String, ConsistentHashSelector> map = new ConcurrentHashMap<>();

    @Override
    protected String doLoadBalanceSelect(List<String> addressList, String rpcServiceName) {
        int hashcode = addressList.hashCode();
        System.out.println(hashcode);
        int consistentHashCode = System.identityHashCode(addressList);
        ConsistentHashSelector selector = map.get(rpcServiceName);
        if (selector == null || selector.identityHashCode != hashcode) {
            map.put(rpcServiceName, new ConsistentHashSelector(addressList, 180, consistentHashCode));
            selector = map.get(rpcServiceName);
        }
        return selector.select(rpcServiceName);
    }


    static class ConsistentHashSelector {
        private final TreeMap<Long, String> virtualInvokers;

        private final int identityHashCode;

        ConsistentHashSelector(List<String> invokers, int replicaNumber, int identityHashCode) {
            this.virtualInvokers = new TreeMap<>();
            this.identityHashCode = identityHashCode;

            for (String invoker : invokers) {
                for (int i = 0; i < replicaNumber / 4; i++) {
                    byte[] digest = md5(invoker + i);
                    for (int h = 0; h < 4; h++) {
                        long m = hash(digest, h);
                        virtualInvokers.put(m, invoker);
                    }
                }
            }
        }

        static byte[] md5(String key) {
            MessageDigest md = null;

            try {
                md = MessageDigest.getInstance("md5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                log.error("An encryption algorithm that does not exist is used: ", e);
                e.printStackTrace();
            }

            return md.digest();
        }

        static long hash(byte[] digest, int idx) {
            idx = new Random().nextInt(4);
            return ((long) (digest[3 + idx * 4] & 255) << 24 | (long) (digest[2 + idx * 4] & 255) << 16 | (long) (digest[1 + idx * 4] & 255) << 8 | (long) (digest[idx * 4] & 255)) & 4294967295L;
        }

        public String select(String rpcServiceName) {
            byte[] digest  = md5(rpcServiceName);
            return selectForKey(hash(digest, 0));
        }

        public String selectForKey(long hashCode) {
            Map.Entry<Long, String> entry = virtualInvokers.tailMap(hashCode, true).firstEntry();

            if (entry == null) {
                entry = virtualInvokers.firstEntry();
            }

            return entry.getValue();
        }
    }
}
