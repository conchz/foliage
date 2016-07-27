package io.foliage.netty.rpc.client

import io.foliage.netty.rpc.registry.ServiceDiscovery

class RpcClient(private val serviceDiscovery: ServiceDiscovery) {

    fun stop() {
        serviceDiscovery.stop()
        RpcProxy.shutdownProxyPool()
        ConnectionManager.getInstance().stop()
    }
}
