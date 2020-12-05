package cn.jw.provider;

import cn.jw.entity.RpcServiceProperties;

public interface ServiceProvider {

    /**
     *  增加rpcService
     * @param service 服务对象
     * @param serviceClass 服务对象的试题类型
     * @param rpcServiceProperties 服务的配置项
     */
    void addService(Object service, Class<?> serviceClass, RpcServiceProperties rpcServiceProperties);

    /**
     *  获得service
     * @param rpcServiceProperties
     * @return
     */
    Object getService(RpcServiceProperties rpcServiceProperties);

    void publishService(Object service, RpcServiceProperties rpcServiceProperties);

    void publishService(Object service);

}
