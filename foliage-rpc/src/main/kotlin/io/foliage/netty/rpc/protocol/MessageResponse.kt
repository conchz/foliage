package io.foliage.netty.rpc.protocol

import org.lavenderx.foliage.nettyrpc.utils.formatToString

open class MessageResponse(val messageId: String) : java.io.Serializable {

    var error: String? = null
    var result: Any? = null

    val isError: Boolean = error != null

    override fun toString(): String {
        return formatToString(this)
    }
}