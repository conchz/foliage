package org.lavenderx.foliage.sample.listener

import org.lavenderx.foliage.nettyrpc.annotation.RpcListenerContainer
import org.lavenderx.foliage.nettyrpc.rpcservice.HelloRpcService
import org.springframework.stereotype.Service

@Service
@RpcListenerContainer(HelloRpcService::class)
class HelloRpcRpcMessageListener : HelloRpcService {

    override fun hello(name: String): String {
        return "Hello, $name!"
    }
}