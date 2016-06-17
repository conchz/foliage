package com.github.lavenderx.netty_rpc.client;

public interface AsyncRpcCallback {

    void success(Object result);

    void fail(Exception ex);

}
