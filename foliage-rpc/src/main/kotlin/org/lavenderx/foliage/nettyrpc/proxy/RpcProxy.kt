package org.lavenderx.foliage.nettyrpc.proxy

import javassist.util.proxy.MethodHandler
import javassist.util.proxy.ProxyFactory
import javassist.util.proxy.ProxyObject
import java.lang.reflect.Method

/**
 * <a href="http://www.cnblogs.com/hujunzheng/p/5134478.html"></a>
 * <a href="http://cuishen.iteye.com/blog/421464"></a>
 */
class RpcProxy : MethodHandler {

    @Throws(Throwable::class)
    override fun invoke(self: Any?, thisMethod: Method?, proceed: Method?, args: Array<out Any>?): Any {
        throw UnsupportedOperationException("not implemented")
    }

    companion object {
        private val proxyFactory = ProxyFactory()

        @Suppress("UNCHECKED_CAST")
        @Throws(Exception::class)
        fun <T : Any> create(interfaceClass: Class<T>): T {
//            proxyFactory.superclass = interfaceClass
            proxyFactory.interfaces = arrayOf(interfaceClass)

            val dynamicClass = proxyFactory.createClass()
            val newInstance = dynamicClass.newInstance() as T
            (newInstance as ProxyObject).handler = MethodHandler { self, thisMethod, proceed, args ->
                proceed.invoke(newInstance, args)
            }

            return newInstance
        }
    }
}