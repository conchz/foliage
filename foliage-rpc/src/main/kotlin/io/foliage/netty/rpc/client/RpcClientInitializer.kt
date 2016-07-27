package io.foliage.netty.rpc.client

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.foliage.netty.rpc.protocol.RpcDecoder
import io.foliage.netty.rpc.protocol.RpcEncoder
import io.foliage.netty.rpc.protocol.RpcRequest
import io.foliage.netty.rpc.protocol.RpcResponse

class RpcClientInitializer : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        ch.pipeline()
                .addLast(RpcEncoder(RpcRequest::class.java))
                .addLast(LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                .addLast(RpcDecoder(RpcResponse::class.java))
                .addLast(RpcClientHandler())
    }
}