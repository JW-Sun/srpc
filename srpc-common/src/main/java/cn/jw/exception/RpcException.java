package cn.jw.exception;

import cn.jw.enums.RpcErrorMessageEnum;

public class RpcException extends RuntimeException {

    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum) {
        super(rpcErrorMessageEnum.getMessage());
    }

    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum, String detail) {
        super(rpcErrorMessageEnum.getMessage() + "- detail: -" + detail);
    }

    public RpcException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
