package cn.jw.registry.zk;

import cn.jw.enums.RpcErrorMessageEnum;
import cn.jw.exception.RpcException;
import cn.jw.extension.ExtensionLoader;
import cn.jw.loadbalance.LoadBalance;
import cn.jw.registry.ServiceDiscover;
import cn.jw.registry.zk.util.CuratorUtil;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

public class ZkServiceDiscovery implements ServiceDiscover {

    private final LoadBalance loadBalance;

    public ZkServiceDiscovery() {
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("loadBalance");
    }

    /**
     *  在zookeeper注册中心中获得serviceName下的子路径
     *
     *  根据路径在总目录下 my-rpc 的目录下，根据服务的名称获得可以运行的host：port
     *
     * @param rpcServiceName
     * @return
     */
    @Override
    public InetSocketAddress lookupService(String rpcServiceName) {
        CuratorFramework zkClient = CuratorUtil.getZkClient();
        List<String> childrenNodes = CuratorUtil.getChildrenNodes(zkClient, rpcServiceName);
        if (childrenNodes == null || childrenNodes.size() == 0) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
        }
        String serviceAddress = loadBalance.getServiceAddress(childrenNodes, rpcServiceName);
        String[] split = serviceAddress.split(":");
        String host = split[0];
        Integer port = Integer.parseInt(split[1]);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
        return inetSocketAddress;
    }
}
