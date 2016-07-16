package org.lavenderx.foliage.nettyrpc.client

import org.lavenderx.foliage.nettyrpc.registry.ServiceDiscovery
import java.lang.reflect.Proxy
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class RpcClient(val serviceDiscovery: ServiceDiscovery) {

    fun stop() {
        shutdownRpcProxyThreadPool()
        this.serviceDiscovery.stop()
        ConnectionManager.getInstance().stop()
    }

    companion object {
        private val threadPoolExecutor =
                ThreadPoolExecutor(16, 16, 10, TimeUnit.MINUTES, ArrayBlockingQueue<Runnable>(65536))

        @Suppress("UNCHECKED_CAST")
        fun <T> create(interfaceClass: Class<T>): T {
            return Proxy.newProxyInstance(
                    interfaceClass.classLoader,
                    arrayOf<Class<*>>(interfaceClass),
                    RpcProxy(interfaceClass)) as T
        }

        fun <T> createAsync(interfaceClass: Class<T>): RpcProxy<T> {
            return RpcProxy(interfaceClass)
        }

        fun submitTaskToRpcProxyThreadPool(task: Runnable) {
            threadPoolExecutor.submit(task)
        }

        fun shutdownRpcProxyThreadPool() {
            threadPoolExecutor.shutdown()
        }
    }
}
