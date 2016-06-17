package com.github.lavenderx.netty_rpc.protocol;

import lombok.Data;

import java.io.Serializable;

@Data
public class RpcResponse implements Serializable {
    private static final long serialVersionUID = 1691010765111001804L;

    private String requestId;
    private String error;
    private Object result;

    public boolean isError() {
        return error != null;
    }
}
