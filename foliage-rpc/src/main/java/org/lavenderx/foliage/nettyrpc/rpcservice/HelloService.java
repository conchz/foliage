package org.lavenderx.foliage.nettyrpc.rpcservice;

import org.lavenderx.foliage.nettyrpc.server.RpcService;

@RpcService
public interface HelloService {

    String hello(String name);
}
