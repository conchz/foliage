package org.lavenderx.foliage.nettyrpc.rpcservice

import org.lavenderx.foliage.nettyrpc.annotation.RpcService

@RpcService
interface HelloRpcService {

    fun hello(name: String): String
}