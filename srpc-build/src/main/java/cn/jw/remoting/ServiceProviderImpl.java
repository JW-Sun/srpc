package cn.jw.remoting;

import cn.jw.entity.RpcServiceProperties;
import cn.jw.extension.ExtensionLoader;
import cn.jw.provider.ServiceProvider;

import cn.jw.registry.ServiceRegistry;
import cn.jw.remoting.transport.netty.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ServiceProviderImpl implements ServiceProvider {

    /**
     *  服务serviceMap的组成
     *  key：service name + version + group
     *  value: service object
     *
     */
    private final Map<String, Object> serviceMap;
    private final Set<String> registeredService;
    private final ServiceRegistry serviceRegistry;

    public ServiceProviderImpl() {
        this.serviceMap = new ConcurrentHashMap<>();
        this.registeredService = ConcurrentHashMap.newKeySet();
        // 就是获得一个接口的实现类实例
        this.serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("zk");
    }

    @Override
    public void addService(Object service, Class<?> serviceClass, RpcServiceProperties rpcServiceProperties) {
        String serviceName = rpcServiceProperties.getServiceName();
        if (registeredService.contains(serviceName)) {
            return;
        }
        registeredService.add(serviceName);
        serviceMap.put(serviceName, service);
        log.info("添加服务：{}, 服务的接口：{}", serviceName, service.getClass().getInterfaces());
    }

    @Override
    public Object getService(RpcServiceProperties rpcServiceProperties) {
        Object service = serviceMap.get(rpcServiceProperties.getServiceName());
        if (service == null) {
            log.error("没有找到service服务");
        }
        return service;
    }

    @Override
    public void publishService(Object service, RpcServiceProperties rpcServiceProperties) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            Class<?> anInterface = service.getClass().getInterfaces()[0];
            String serviceInterfaceName = anInterface.getCanonicalName();

            // 开始添加的工作
            rpcServiceProperties.setServiceName(serviceInterfaceName);
            this.addService(service, anInterface, rpcServiceProperties);
            serviceRegistry.serviceRegistry(serviceInterfaceName, new InetSocketAddress(host, NettyRpcServer.PORT));
        } catch (UnknownHostException e) {
            log.error("主机无法识别的问题");
        } finally {
        }
    }

    @Override
    public void publishService(Object service) {
        this.publishService(service, RpcServiceProperties.builder().group("").version("").build());
    }
}
