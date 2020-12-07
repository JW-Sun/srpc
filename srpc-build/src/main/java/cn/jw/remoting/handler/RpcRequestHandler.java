package cn.jw.remoting.handler;

import cn.jw.factory.SingletonFactory;
import cn.jw.provider.ServiceProvider;
import cn.jw.remoting.ServiceProviderImpl;
import cn.jw.remoting.dto.RpcRequest;
import cn.jw.remoting.dto.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class RpcRequestHandler {

    private ServiceProvider serviceProvider;

    public RpcRequestHandler() {
        this.serviceProvider = SingletonFactory.getSingleton(ServiceProviderImpl.class);
    }

    public Object handle(RpcRequest rpcRequest) {
        // 在提供服务的地方查询是否有对应的服务
        Object service = serviceProvider.getService(rpcRequest.toRpcProperties());
        if (service == null) {
            log.error("反射调用阶段，该服务并没有进行发布注册：【{}】", rpcRequest.toRpcProperties().getServiceName());
        }
        return invokeHandlerFunction(rpcRequest, service);
    }

    /**
     *  反射调用服务中对应的方法
     * @param rpcRequest
     * @param service
     * @return
     */
    private Object invokeHandlerFunction(RpcRequest rpcRequest, Object service) {
        Object invoke = null;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            invoke = method.invoke(service, rpcRequest.getParameters());
            log.info("请求反射处理服务:[{}], 成功调用方法:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("反射调用方法抛出的异常 : [{}]", e.getMessage());
        }
        return invoke;
    }
}
