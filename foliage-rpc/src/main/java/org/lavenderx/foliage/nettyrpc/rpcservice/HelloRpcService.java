package org.lavenderx.foliage.nettyrpc.rpcservice;

import org.lavenderx.foliage.nettyrpc.annotation.RpcService;

@RpcService
public interface HelloRpcService {

    String hello(String name);
}
