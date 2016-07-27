package io.foliage.netty.rpc.core

import io.foliage.netty.rpc.protocol.MessageRequest
import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy
import java.lang.reflect.Method
import java.util.*

class MessageSendProxy<T>(val clazz: Class<T>) : MethodInterceptor {

    override fun intercept(obj: Any, method: Method, args: Array<out Any>, proxy: MethodProxy): Any {
        val request = MessageRequest(UUID.randomUUID().toString().replace("-", ""))
        return request
    }
}