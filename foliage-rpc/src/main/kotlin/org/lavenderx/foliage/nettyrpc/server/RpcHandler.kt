package org.lavenderx.foliage.nettyrpc.server

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.sf.cglib.reflect.FastClass
import org.lavenderx.foliage.nettyrpc.logging.loggerFor
import org.lavenderx.foliage.nettyrpc.protocol.RpcRequest
import org.lavenderx.foliage.nettyrpc.protocol.RpcResponse

class RpcHandler(private val handlerMap: Map<String, Any>) : SimpleChannelInboundHandler<RpcRequest>() {

    private val logger = loggerFor<RpcHandler>()

    @Throws(Exception::class)
    public override fun channelRead0(ctx: ChannelHandlerContext, request: RpcRequest) {
        RpcServer.submit(Runnable {
            logger.debug("Receive request {}", request.requestId)
            val response = RpcResponse(request.requestId)
            try {
                val result = handle(request)
                response.result = result
            } catch (t: Throwable) {
                response.error = t.toString()
                logger.error("RPC Server handle request error", t)
            }

            ctx.writeAndFlush(response).addListener {
                logger.debug("Send response for request {}", request.requestId)
            }
        })
    }

    @Throws(Throwable::class)
    private fun handle(request: RpcRequest): Any {
        val className = request.className
        val serviceBean = handlerMap[className]

        val serviceClass = serviceBean!!.javaClass
        val methodName = request.methodName
        val parameterTypes = request.parameterTypes
        val parameters = request.parameters

        logger.debug(serviceClass.name)
        logger.debug(methodName)
        for (parameterType in parameterTypes!!) {
            logger.debug(parameterType.name)
        }
        for (parameter in parameters!!) {
            logger.debug(parameter.toString())
        }

        // Cglib reflect
        val serviceFastClass = FastClass.create(serviceClass)
        val serviceFastMethod = serviceFastClass.getMethod(methodName!!, parameterTypes)
        return serviceFastMethod.invoke(serviceBean, parameters)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        logger.error("Server caught exception", cause)
        ctx.close()
    }
}