package io.foliage.netty.rpc.protocol

import org.apache.commons.lang3.builder.ToStringStyle
import org.lavenderx.foliage.nettyrpc.utils.formatToString

enum class RpcSerializeProtocol(val serializeProtocol: String) {

    JDK_SERIALIZATION("jdknative"), KRYO_SERIALIZATION("kryo");

    fun getProtocol(): String {
        return serializeProtocol
    }

    override fun toString(): String {
        return formatToString(this, ToStringStyle.SHORT_PREFIX_STYLE)
    }
}