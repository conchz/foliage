package org.lavenderx.foliage.sample.config

import io.foliage.netty.rpc.config.RootConfig
import io.foliage.netty.rpc.registry.ServiceRegistry
import io.foliage.netty.rpc.server.RpcServer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@ComponentScan("org.lavenderx.foliage.sample.listener")
@Import(RootConfig::class)
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
    open fun rpcServer(): RpcServer {
        return RpcServer(serverAddress!!, serviceRegistry())
    }
}