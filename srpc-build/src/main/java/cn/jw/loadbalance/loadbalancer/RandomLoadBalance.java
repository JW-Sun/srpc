package cn.jw.loadbalance.loadbalancer;

import cn.jw.loadbalance.AbstractLoadBalance;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doLoadBalanceSelect(List<String> addressList, String rpcServiceName) {
        Random random = new Random();
        String address = addressList.get(random.nextInt(addressList.size()));
        return address;
    }


}
