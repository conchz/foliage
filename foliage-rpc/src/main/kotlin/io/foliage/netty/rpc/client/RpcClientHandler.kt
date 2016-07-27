package io.foliage.netty.rpc.client

import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.foliage.utils.loggerFor
import io.foliage.netty.rpc.protocol.RpcRequest
import io.foliage.netty.rpc.protocol.RpcResponse
import io.netty.channel.ChannelFutureListener
import java.net.SocketAddress
import java.util.concurrent.ConcurrentHashMap

class RpcClientHandler : SimpleChannelInboundHandler<RpcResponse>() {

    private val logger = loggerFor<RpcClientHandler>()
    private val pendingRpc = ConcurrentHashMap<String, RpcFuture>()

    @Volatile var channel: Channel? = null
    @Volatile var remotePeer: SocketAddress? = null

    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        super.channelActive(ctx)
        this.remotePeer = channel!!.remoteAddress()
    }

    @Throws(Exception::class)
    override fun channelRegistered(ctx: ChannelHandlerContext) {
        super.channelRegistered(ctx)
        this.channel = ctx.channel()
    }

    @Throws(Exception::class)
    public override fun channelRead0(ctx: ChannelHandlerContext, response: RpcResponse) {
        val requestId = response.requestId
        val rpcFuture = pendingRpc[requestId]
        if (rpcFuture != null) {
            pendingRpc.remove(requestId)
            rpcFuture.done(response)
        }
    }

    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        logger.error("client caught exception", cause)
        ctx.close()
    }

    fun close() {
        channel!!.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(io.netty.channel.ChannelFutureListener.CLOSE)
    }

    fun sendRequest(request: RpcRequest): RpcFuture {
        val rpcFuture = RpcFuture(request)
        pendingRpc.put(request.requestId, rpcFuture)
        channel!!.writeAndFlush(request)

        return rpcFuture
    }
}