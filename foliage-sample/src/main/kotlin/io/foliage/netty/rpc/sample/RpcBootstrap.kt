package io.foliage.netty.rpc.sample

import io.foliage.netty.rpc.sample.config.ServerConfig
import org.springframework.context.annotation.AnnotationConfigApplicationContext

class RpcBootstrap {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            val ctx = AnnotationConfigApplicationContext(ServerConfig::class.java)

            Runtime.getRuntime().addShutdownHook(Thread(Runnable { ctx.close() }))
        }
    }
}