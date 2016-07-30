package io.foliage.api.config

import com.google.common.base.CaseFormat
import io.foliage.netty.rpc.annotation.RpcService
import io.foliage.netty.rpc.core.MessageSendExecutor
import io.foliage.netty.rpc.registry.ServiceDiscovery
import io.foliage.utils.loggerFor
import org.reflections.Reflections
import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.Assert
import java.io.IOException
import java.util.*

@Configuration
open class RpcCallableConfig : BeanFactoryPostProcessor {

    private val logger = loggerFor<RpcCallableConfig>()
    private var registryAddress: String? = null
    private var serverAddress: String? = null

    init {
        loadRpcConfig()
        Assert.notNull(registryAddress)
        Assert.notNull(serverAddress)
    }

    @Bean
    open fun serviceDiscovery(): ServiceDiscovery {
        return ServiceDiscovery(registryAddress)
    }

    @Bean
    open fun rpcClient(): MessageSendExecutor {
        return MessageSendExecutor(serverAddress, serviceDiscovery())
    }

    @Throws(BeansException::class)
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        val rpcServicePackages = "io.foliage.netty.rpc.rpcservice"

        logger.info("Initializing RPC client stubs")
        val reflections = Reflections(rpcServicePackages)
        for (clazz in reflections.getTypesAnnotatedWith(RpcService::class.java)) {
            val rpcServiceName = clazz.simpleName
            logger.info("Processing rpc service annotation {}", rpcServiceName)

            // Register rpc stub
            val rpcClientStub = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, clazz.simpleName)

            logger.info("Registering rpc stub {}", rpcClientStub)
            val rpcServiceBean = MessageSendExecutor.execute(clazz)
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
        } catch (ex: IOException) {
            throw RuntimeException("Failed to read RPC config file")
        }

    }
}