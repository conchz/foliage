package org.lavenderx.foliage.api.config

import com.google.common.base.CaseFormat
import org.lavenderx.foliage.nettyrpc.annotation.RpcService
import org.lavenderx.foliage.nettyrpc.client.RpcClient
import org.lavenderx.foliage.nettyrpc.logging.loggerFor
import org.lavenderx.foliage.nettyrpc.registry.ServiceDiscovery
import org.reflections.Reflections
import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.IOException
import java.util.*

@Configuration
open class RpcCallableConfig : BeanFactoryPostProcessor {

    private val logger = loggerFor<RpcCallableConfig>()
    private var registryAddress: String? = null
    private var serverAddress: String? = null

    init {
        loadRpcConfig()
    }

    @Bean
    open fun serviceDiscovery(): ServiceDiscovery {
        return ServiceDiscovery(registryAddress!!)
    }

    @Bean
    open fun rpcClient(): RpcClient {
        return RpcClient(serviceDiscovery())
    }

    @Throws(BeansException::class)
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        val rpcServicePackages = "org.lavenderx.foliage.nettyrpc.rpcservice"

        logger.info("Initializing RPC client stubs")
        val reflections = Reflections(rpcServicePackages)
        for (clazz in reflections.getTypesAnnotatedWith(RpcService::class.java)) {
            val rpcServiceName = clazz.simpleName
            logger.info("Processing rpc service annotation {}", rpcServiceName)

            // Register rpc stub
            val rpcClientStub = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, clazz.simpleName)

            logger.info("Registering rpc stub {}", rpcClientStub)
            val rpcServiceBean = RpcClient.create(clazz)
            beanFactory.registerSingleton(rpcClientStub, clazz.cast(rpcServiceBean))
            beanFactory.initializeBean(clazz.cast(rpcServiceBean), rpcClientStub)

            logger.info("Complete registering rpc service {}", rpcServiceName)
        }
    }

    private fun loadRpcConfig() {
        try {
            javaClass.classLoader.getResourceAsStream("rpc.properties").use { inputStream ->
                val props = Properties()
                props.load(inputStream)

                this.registryAddress = props.getProperty("registry.address")
                this.serverAddress = props.getProperty("server.address")

            }
        } catch (e: IOException) {
            throw RuntimeException("Failed to read RPC config file")
        }

    }
}