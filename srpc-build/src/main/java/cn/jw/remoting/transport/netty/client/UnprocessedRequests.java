package cn.jw.remoting.transport.netty.client;

import cn.jw.remoting.dto.RpcRequest;
import cn.jw.remoting.dto.RpcResponse;
import com.sun.xml.internal.ws.util.CompletedFuture;


import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  服务端没有处理的请求
 */
public class UnprocessedRequests {

    private static final Map<String, CompletableFuture<RpcResponse<Object>>> map = new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<RpcResponse<Object>> future) {
        map.put(requestId, future);
    }

    /**
     *  complete的动作
     *
     *  具体作用是先保存客户端发往服务端的request，以及对应的response
     *  在ClientHandler中接收到server返回的消息通过这个complete方法进行返回
     * @param rpcResponse
     */
    public void complete(RpcResponse<Object> rpcResponse) {
        CompletableFuture<RpcResponse<Object>> remove = map.remove(rpcResponse.getRequestId());
        if (remove != null) {
            remove.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }

}
