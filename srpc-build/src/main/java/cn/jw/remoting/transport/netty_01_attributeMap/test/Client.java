package cn.jw.remoting.transport.netty_01_attributeMap.test;

import cn.jw.remoting.dto.RpcRequest;
import cn.jw.remoting.dto.RpcResponse;
import cn.jw.remoting.transport.netty_01_attributeMap.NettyClient;

public class Client {
    public static void main(String[] args) {
        RpcRequest rpcRequest = RpcRequest.builder()
                .interfaceName("interface")
                .methodName("hello").build();

        NettyClient client = new NettyClient("localhost", 8889);

        for (int i = 0; i < 100; i++) {
            client.sendMessage(rpcRequest);
        }

        RpcResponse response = client.sendMessage(rpcRequest);
        System.out.println(response.toString());
    }
}
