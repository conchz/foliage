package io.foliage.netty.rpc.client

import io.foliage.netty.rpc.protocol.RpcRequest
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class RpcProxy<out T : Any>(private val clazz: Class<T>) : InvocationHandler {

    @Throws(Throwable::class)
    override fun invoke(proxy: Any, method: Method, args: Array<Any>): Any {
        if (proxy.javaClass == method.declaringClass) {
            val name = method.name
            if ("equals" == name) {
                return proxy === args[0]
            } else if ("hashCode" == name) {
                return System.identityHashCode(proxy)
            } else if ("toString" == name) {
                return proxy.javaClass.name + "@" + Integer.toHexString(System.identityHashCode(proxy)) + ", with InvocationHandler " + this
            } else {
                throw IllegalStateException(method.toString())
            }
        }

        val request = RpcRequest(UUID.randomUUID().toString())
        request.className = method.declaringClass.name
        request.methodName = method.name
        request.parameterTypes = method.parameterTypes
        request.parameters = args

        val handler = ConnectionManager.getInstance().chooseHandler()
        val rpcFuture = handler.sendRequest(request)

        return rpcFuture.get()!!
    }

    @Suppress("UNCHECKED_CAST")
    fun call(funcName: String, vararg args: Any): RpcFuture {
        val handler = ConnectionManager.getInstance().chooseHandler()
        val request = createRequest(this.clazz.name, funcName, args as Array<Any>)
        val rpcFuture = handler.sendRequest(request)

        return rpcFuture
    }

    private fun createRequest(className: String, methodName: String, args: Array<Any>): RpcRequest {
        val request = RpcRequest(UUID.randomUUID().toString())
        request.className = className
        request.methodName = methodName
        request.parameters = args

        val parameterTypes = arrayOfNulls<Class<*>>(args.size)
        // Get the right class type
        var i = 0
        val l = args.size
        while (i < l) {
            parameterTypes[i] = getClassType(args[i])
            i++
        }
        request.parameterTypes = parameterTypes.filterNotNull().toTypedArray()

        return request
    }

    private fun getClassType(obj: Any): Class<*> {
        val classType = obj.javaClass
        val typeNamedClass = when (classType.name) {
            "java.lang.Integer" -> Integer.TYPE
            "java.lang.Long" -> java.lang.Long.TYPE
            "java.lang.Float" -> java.lang.Float.TYPE
            "java.lang.Double" -> java.lang.Double.TYPE
            "java.lang.Character" -> Character.TYPE
            "java.lang.Boolean" -> java.lang.Boolean.TYPE
            "java.lang.Short" -> java.lang.Short.TYPE
            "java.lang.Byte" -> java.lang.Byte.TYPE
            "kotlin.Int" -> Int::class.java
            "kotlin.Long" -> Long::class.java
            "kotlin.Float" -> Float::class.java
            "kotlin.Double" -> Double::class.java
            "kotlin.Char" -> Char::class.java
            "kotlin.Boolean" -> Boolean::class.java
            "kotlin.Short" -> Short::class.java
            "kotlin.Byte" -> Byte::class.java
            else -> null
        }

        if (typeNamedClass != null) {
            return typeNamedClass
        } else {
            return classType
        }
    }

    companion object {
        private val threadPoolExecutor =
                ThreadPoolExecutor(16, 16, 10, TimeUnit.MINUTES, ArrayBlockingQueue<Runnable>(65536))

        @Suppress("UNCHECKED_CAST")
        fun <T : Any> create(interfaceClass: Class<T>): T {
            return Proxy.newProxyInstance(
                    interfaceClass.classLoader,
                    arrayOf(interfaceClass),
                    RpcProxy(interfaceClass)) as T
        }

        fun <T : Any> createAsync(interfaceClass: Class<T>): RpcProxy<T> {
            return RpcProxy(interfaceClass)
        }

        fun submitTaskToProxyPool(task: Runnable) {
            threadPoolExecutor.submit(task)
        }

        fun shutdownProxyPool() {
            threadPoolExecutor.shutdown()
        }
    }
}