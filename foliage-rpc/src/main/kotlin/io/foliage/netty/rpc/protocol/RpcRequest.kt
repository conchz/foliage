package io.foliage.netty.rpc.protocol

import java.io.Serializable

data class RpcRequest(val requestId: String) : Serializable {

    var className: String? = null
    var methodName: String? = null
    var parameterTypes: Array<Class<*>>? = null
    var parameters: Array<Any>? = null

    companion object {
        private val serialVersionUID = -2073278080248168261L
    }
}