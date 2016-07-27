package io.foliage.netty.rpc.sample.config

import io.foliage.netty.rpc.config.RpcConfig
import io.foliage.netty.rpc.core.MessageReceiveExecutor
import io.foliage.netty.rpc.registry.ServiceRegistry
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@ComponentScan("io.foliage.netty.rpc.sample.listener")
@Import(RpcConfig::class)
open class ServerConfig {

    @Value("\${registry.address}")
    private val registryAddress: String? = null

    @Value("\${server.address}")
    private val serverAddress: String? = null

    @Bean
    open fun serviceRegistry(): ServiceRegistry {
        return ServiceRegistry(registryAddress!!)
    }

    @Bean
    open fun rpcServer(): MessageReceiveExecutor {
        return MessageReceiveExecutor(serverAddress!!)
    }
}