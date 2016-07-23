package org.lavenderx.foliage.nettyrpc.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import org.lavenderx.foliage.nettyrpc.annotation.RpcListenerContainer
import org.lavenderx.foliage.nettyrpc.utils.loggerFor
import org.lavenderx.foliage.nettyrpc.protocol.RpcDecoder
import org.lavenderx.foliage.nettyrpc.protocol.RpcEncoder
import org.lavenderx.foliage.nettyrpc.protocol.RpcRequest
import org.lavenderx.foliage.nettyrpc.protocol.RpcResponse
import org.lavenderx.foliage.nettyrpc.registry.ServiceRegistry
import org.springframework.beans.BeansException
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class RpcServer(private val serverAddress: String,
                private val serviceRegistry: ServiceRegistry) : ApplicationContextAware, InitializingBean {

    private val logger = loggerFor<RpcServer>()
    private val handlerMap = HashMap<String, Any>()

    @Throws(BeansException::class)
    override fun setApplicationContext(ctx: ApplicationContext) {
        val serviceBeanMap = ctx.getBeansWithAnnotation(RpcListenerContainer::class.java)
        if (serviceBeanMap.isNotEmpty()) {
            for (serviceBean in serviceBeanMap.values) {
                val interfaceName = serviceBean?.javaClass?.
                        getAnnotation(RpcListenerContainer::class.java)?.value?.qualifiedName!!

                handlerMap.put(interfaceName, serviceBean)
            }
        }
    }

    @Throws(Exception::class)
    override fun afterPropertiesSet() {
        val bossGroup = NioEventLoopGroup()
        val workerGroup = NioEventLoopGroup()
        try {
            val bootstrap = ServerBootstrap()
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel::class.java)
                    .childHandler(object : ChannelInitializer<SocketChannel>() {
                        @Throws(Exception::class)
                        public override fun initChannel(channel: SocketChannel) {
                            channel.pipeline()
                                    .addLast(LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                                    .addLast(RpcDecoder(RpcRequest::class.java))
                                    .addLast(RpcEncoder(RpcResponse::class.java))
                                    .addLast(RpcHandler(handlerMap))
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true)

            val array = serverAddress.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val host = array[0]
            val port = array[1].toInt()

            val future = bootstrap.bind(host, port).sync()
            logger.info("RPC Server started on port {}", port)

            serviceRegistry.register(serverAddress)

            future.channel().closeFuture().sync()
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
        }
    }

    companion object {

        @Volatile private var threadPoolExecutor: ThreadPoolExecutor? = null

        fun submit(task: Runnable) {
            if (threadPoolExecutor == null) {
                synchronized(RpcServer::class.java) {
                    if (threadPoolExecutor == null) {
                        threadPoolExecutor = ThreadPoolExecutor(16, 16, 10, TimeUnit.MINUTES, ArrayBlockingQueue<Runnable>(65536))
                    }
                }
            }
            threadPoolExecutor!!.submit(task)
        }
    }
}