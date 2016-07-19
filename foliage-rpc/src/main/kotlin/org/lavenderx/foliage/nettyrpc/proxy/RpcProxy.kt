package org.lavenderx.foliage.nettyrpc.proxy

import javassist.util.proxy.ProxyFactory

object RpcProxy {

    private val proxyFactory = ProxyFactory()

    @Throws(Exception::class)
    fun <T> create(clazz: Class<T>) {
    }
}