package com.github.lavenderx.netty_rpc.client.proxy;

import com.github.lavenderx.netty_rpc.client.RpcFuture;

@FunctionalInterface
public interface IAsyncProxy {

    RpcFuture call(String funcName, Object... args);
}