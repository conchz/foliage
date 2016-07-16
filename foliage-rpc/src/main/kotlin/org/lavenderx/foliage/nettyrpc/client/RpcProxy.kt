package org.lavenderx.foliage.nettyrpc.client

import org.lavenderx.foliage.nettyrpc.logging.loggerFor
import org.lavenderx.foliage.nettyrpc.protocol.RpcRequest
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.util.*

class RpcProxy<T>(val clazz: Class<T>) : InvocationHandler {

    private val logger = loggerFor<RpcProxy<T>>()

    @Throws(Throwable::class)
    override fun invoke(proxy: Any, method: Method, args: Array<Any>): Any {
        if (Any::class.java == method.declaringClass) {
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

        // Debug
        logger.debug(method.declaringClass.name)
        logger.debug(method.name)
        var i = 0
        val l = method.parameterTypes.size
        while (i < l) {
            logger.debug(method.parameterTypes[i].name)
            ++i
        }

        for (arg in args) {
            logger.debug(arg.toString())
        }

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

        logger.debug(className)
        logger.debug(methodName)
        for (parameterType in parameterTypes) {
            logger.debug(parameterType!!.name)
        }
        for (arg in args) {
            logger.debug(arg.toString())
        }

        return request
    }

    private fun getClassType(obj: Any): Class<*> {
        val classType = obj.javaClass
        val typeName = classType.name
        when (typeName) {
            "java.lang.Integer" -> return Integer.TYPE
            "java.lang.Long" -> return java.lang.Long.TYPE
            "java.lang.Float" -> return java.lang.Float.TYPE
            "java.lang.Double" -> return java.lang.Double.TYPE
            "java.lang.Character" -> return Character.TYPE
            "java.lang.Boolean" -> return java.lang.Boolean.TYPE
            "java.lang.Short" -> return java.lang.Short.TYPE
            "java.lang.Byte" -> return java.lang.Byte.TYPE
        }

        return classType
    }
}