package org.lavenderx.foliage.nettyrpc.client

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import org.lavenderx.foliage.nettyrpc.protocol.RpcDecoder
import org.lavenderx.foliage.nettyrpc.protocol.RpcEncoder
import org.lavenderx.foliage.nettyrpc.protocol.RpcRequest
import org.lavenderx.foliage.nettyrpc.protocol.RpcResponse

class RpcClientInitializer : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        ch.pipeline()
                .addLast(RpcEncoder(RpcRequest::class.java))
                .addLast(LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                .addLast(RpcDecoder(RpcResponse::class.java))
                .addLast(RpcClientHandler())
    }
}