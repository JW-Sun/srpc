package cn.jw.loadbalance;

import cn.jw.extension.SPI;

import java.util.List;

@SPI
public interface LoadBalance {

    public String getServiceAddress(List<String> addressList, String rpcServiceName);

}
