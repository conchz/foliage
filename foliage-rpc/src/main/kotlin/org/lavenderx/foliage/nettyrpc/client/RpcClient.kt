package org.lavenderx.foliage.nettyrpc.client

import org.lavenderx.foliage.nettyrpc.registry.ServiceDiscovery

class RpcClient(private val serviceDiscovery: ServiceDiscovery) {

    fun stop() {
        serviceDiscovery.stop()
        RpcProxy.shutdownProxyPool()
        ConnectionManager.getInstance().stop()
    }
}
