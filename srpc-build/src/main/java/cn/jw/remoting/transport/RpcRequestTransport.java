package cn.jw.remoting.transport;

import cn.jw.remoting.dto.RpcRequest;

import java.util.concurrent.ExecutionException;

public interface RpcRequestTransport {
    Object sendRpcRequest(RpcRequest rpcRequest) throws ExecutionException, InterruptedException;
}
