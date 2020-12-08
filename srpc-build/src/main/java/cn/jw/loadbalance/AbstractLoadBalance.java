package cn.jw.loadbalance;

import java.util.List;

/**
 *  负载均衡抽象类
 */
public abstract class AbstractLoadBalance implements LoadBalance {

    @Override
    public String getServiceAddress(List<String> addressList, String rpcServiceName) {
        if (addressList == null || addressList.size() == 0) {
            return null;
        }
        if (addressList.size() == 1) {
            return addressList.get(0);
        }
        return doLoadBalanceSelect(addressList, rpcServiceName);
    }

    protected abstract String doLoadBalanceSelect(List<String> addressList, String rpcServiceName);

}
