package org.lavenderx.foliage.sample.listener

import io.foliage.netty.rpc.annotation.RpcListenerContainer
import io.foliage.netty.rpc.rpcservice.HelloRpcService
import org.springframework.stereotype.Service

@Service
@RpcListenerContainer(HelloRpcService::class)
class HelloRpcRpcMessageListener : HelloRpcService {

    override fun hello(name: String): String {
        return "Hello, $name!"
    }
}