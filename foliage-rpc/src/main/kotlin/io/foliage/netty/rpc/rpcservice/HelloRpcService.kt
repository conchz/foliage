package io.foliage.netty.rpc.rpcservice

import io.foliage.netty.rpc.annotation.RpcService

@RpcService
interface HelloRpcService {

    fun hello(name: String): String
}