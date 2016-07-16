package org.lavenderx.foliage.nettyrpc.protocol

import java.io.Serializable

data class RpcResponse(val requestId: String) : Serializable {

    var error: String? = null
    var result: Any? = null

    val isError: Boolean = error == null

    companion object {
        private val serialVersionUID = 1691010765111001804L
    }
}