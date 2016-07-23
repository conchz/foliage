package io.foliage.netty.rpc.protocol

import org.lavenderx.foliage.nettyrpc.utils.formatToString

open class MessageRequest(val messageId: String) : java.io.Serializable {

    val className: String? = null
    val methodName: String? = null
    val parameterTypes: Array<Class<*>>? = null
    val parameterValues: Array<Any>? = null

    override fun toString(): String {
        return formatToString(this)
    }
}