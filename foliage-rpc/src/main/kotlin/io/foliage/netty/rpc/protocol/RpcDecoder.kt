package io.foliage.netty.rpc.protocol

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.foliage.utils.SerializationUtils

class RpcDecoder(private val genericClass: Class<*>) : ByteToMessageDecoder() {

    @Throws(Exception::class)
    public override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any>) {
        if (`in`.readableBytes() < 4) {
            return
        }
        `in`.markReaderIndex()
        val dataLength = `in`.readInt()

        if (`in`.readableBytes() < dataLength) {
            `in`.resetReaderIndex()
            return
        }
        val data = ByteArray(dataLength)
        `in`.readBytes(data)

        val obj = SerializationUtils.deserialize(data, genericClass)
        out.add(obj)
    }

}