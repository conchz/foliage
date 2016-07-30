package io.foliage.netty.rpc.sample.config

import io.foliage.netty.rpc.config.RpcConfig
import io.foliage.netty.rpc.core.MessageReceiveExecutor
import io.foliage.netty.rpc.registry.ServiceRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@ComponentScan("io.foliage.netty.rpc.sample.listener")
@Import(RpcConfig::class)
open class ServerConfig {

    private val registryAddress: String
    private val serverAddress: String

    @Autowired constructor(@Value("\${registry.address}") registryAddress: String,
                           @Value("\${server.address}") serverAddress: String) {
        this.registryAddress = registryAddress
        this.serverAddress = serverAddress
    }

    @Bean
    open fun serviceRegistry(): ServiceRegistry {
        return ServiceRegistry(registryAddress)
    }

    @Bean
    open fun rpcServer(): MessageReceiveExecutor {
        return MessageReceiveExecutor(serverAddress, serviceRegistry())
    }
}