package io.foliage.netty.rpc.sample.listener

import io.foliage.netty.rpc.annotation.RpcListenerContainer
import io.foliage.netty.rpc.sample.rpcservice.HelloRpcService
import org.springframework.stereotype.Service

@Service
@RpcListenerContainer(HelloRpcService::class)
class HelloRpcRpcMessageListener : HelloRpcService {

    override fun hello(name: String): String {
        return "Hello, $name!"
    }
}