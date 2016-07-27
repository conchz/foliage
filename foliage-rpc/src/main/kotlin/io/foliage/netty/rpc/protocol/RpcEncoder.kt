package io.foliage.netty.rpc.protocol

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import io.foliage.utils.SerializationUtils

class RpcEncoder<T : Any>(private val genericClass: Class<T>) : MessageToByteEncoder<Any>() {

    @Throws(Exception::class)
    override fun encode(ctx: ChannelHandlerContext, msg: Any, out: ByteBuf) {
        if (genericClass.isInstance(msg)) {
            val data = SerializationUtils.serialize(msg)
            out.writeInt(data.size)
            out.writeBytes(data)
        }
    }
}