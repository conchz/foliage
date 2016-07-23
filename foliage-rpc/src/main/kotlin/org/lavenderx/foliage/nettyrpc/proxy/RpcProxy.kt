package org.lavenderx.foliage.nettyrpc.proxy

import net.sf.cglib.proxy.Enhancer
import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy
import java.lang.reflect.Method

class RpcProxy : MethodInterceptor {

    private val enhancer = Enhancer()
    fun getProxy(interfaceClass: Class<*>): Any {
        enhancer.setSuperclass(interfaceClass)
        enhancer.setCallback(this)
        return enhancer.create()
    }


    @Throws(Throwable::class)
    override fun intercept(obj: Any, method: Method, args: Array<out Any>, proxy: MethodProxy): Any {
        val result: Any = proxy.invoke(obj, args)

        return result
    }

    companion object {
        private val rpcProxy = RpcProxy()

        @Suppress("UNCHECKED_CAST")
        fun <T : Any> create(interfaceClass: Class<T>): T {
            return rpcProxy.getProxy(interfaceClass) as T
        }
    }
}