package io.foliage.netty.rpc.sample.rpcservice

import io.foliage.netty.rpc.annotation.RpcService

@RpcService
interface HelloRpcService {

    fun hello(name: String): String
}