package com.github.lavenderx.netty_rpc.client.proxy;

import com.github.lavenderx.netty_rpc.client.RpcFuture;

public interface IAsyncObjectProxy {

    RpcFuture call(String funcName, Object... args);
}