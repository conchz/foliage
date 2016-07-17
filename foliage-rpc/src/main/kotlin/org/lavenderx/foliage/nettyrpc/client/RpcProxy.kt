package org.lavenderx.foliage.nettyrpc.client

import org.lavenderx.foliage.nettyrpc.protocol.RpcRequest
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.util.*

class RpcProxy<T>(val clazz: Class<T>) : InvocationHandler {

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
            "java.lang.Character" -> java.lang.Character.TYPE
            "java.lang.Boolean" -> java.lang.Boolean.TYPE
            "java.lang.Short" -> java.lang.Short.TYPE
            "java.lang.Byte" -> java.lang.Byte.TYPE
            "kotlin.Int" -> kotlin.Int::class.java
            "kotlin.Long" -> kotlin.Long::class.java
            "kotlin.Float" -> kotlin.Float::class.java
            "kotlin.Double" -> kotlin.Double::class.java
            "kotlin.Char" -> kotlin.Char::class.java
            "kotlin.Boolean" -> kotlin.Boolean::class.java
            "kotlin.Short" -> kotlin.Short::class.java
            "kotlin.Byte" -> kotlin.Byte::class.java
            else -> null
        }

        if (typeNamedClass != null) {
            return typeNamedClass
        } else {
            return classType
        }
    }
}