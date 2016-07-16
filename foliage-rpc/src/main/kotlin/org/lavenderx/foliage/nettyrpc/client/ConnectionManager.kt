package org.lavenderx.foliage.nettyrpc.client

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import org.lavenderx.foliage.nettyrpc.logging.loggerFor
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

open class ConnectionManager internal constructor() {

    private val logger = loggerFor<ConnectionManager>()

    private val eventLoopGroup = NioEventLoopGroup(4)
    private val threadPoolExecutor = ThreadPoolExecutor(16, 16, 10, TimeUnit.MINUTES, ArrayBlockingQueue<Runnable>(65536))

    private val connectedHandlers = CopyOnWriteArrayList<RpcClientHandler>()
    private val connectedServerNodes = ConcurrentHashMap<InetSocketAddress, RpcClientHandler>()

    private val lock = ReentrantLock()
    private val connected = lock.newCondition()
    private val roundRobin = AtomicInteger(0)
    private val isRunning = AtomicBoolean(true)
    private val connectTimeoutMillis: Long = 6000

    fun updateConnectedServer(allServerAddress: List<String>?) {
        if (allServerAddress != null) {
            if (allServerAddress.size > 0) {  // Get available server node
                // Update local serverNodes cache
                val newAllServerNodeSet = HashSet<InetSocketAddress>()
                for (address in allServerAddress) {
                    val array = address.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (array.size == 2) { // Should check IP and port
                        val host = array[0]
                        val port = Integer.parseInt(array[1])
                        val remotePeer = InetSocketAddress(host, port)
                        newAllServerNodeSet.add(remotePeer)
                    }
                }

                // Add new server node
                newAllServerNodeSet
                        .filter { serverNodeAddress -> !connectedServerNodes.keys.contains(serverNodeAddress) }
                        .forEach { address -> this.connectServerNode(address) }

                // Close and remove invalid server nodes
                for (i in connectedHandlers.indices) {
                    val connectedServerHandler = connectedHandlers[i]
                    val remotePeer = connectedServerHandler.remotePeer
                    if (!newAllServerNodeSet.contains(remotePeer)) {
                        logger.info("Remove invalid server node " + remotePeer)
                        val handler = connectedServerNodes[remotePeer]
                        handler!!.close()
                        connectedServerNodes.remove(remotePeer)
                        connectedHandlers.remove(connectedServerHandler)
                    }
                }

            } else { // No available server node ( All server nodes are down )
                logger.error("No available server node. All server nodes are down!")
                for (connectedServerHandler in connectedHandlers) {
                    val remotePeer = connectedServerHandler.remotePeer
                    val handler = connectedServerNodes[remotePeer]
                    handler!!.close()
                    connectedServerNodes.remove(connectedServerHandler.remotePeer)
                }
                connectedHandlers.clear()
            }
        }
    }

    fun reconnect(handler: RpcClientHandler?, remotePeer: SocketAddress) {
        if (handler != null) {
            connectedHandlers.remove(handler)
            connectedServerNodes.remove(handler.remotePeer)
        }

        connectServerNode(remotePeer as InetSocketAddress)
    }

    private fun connectServerNode(remotePeer: InetSocketAddress) {
        threadPoolExecutor.submit({
            val bootstrap = Bootstrap()
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel::class.java).handler(RpcClientInitializer())

            val channelFuture = bootstrap.connect(remotePeer)
            channelFuture.addListener { channelFuture ->
                if (channelFuture.isSuccess) {
                    logger.debug("Successfully connect to remote server. remote peer = " + remotePeer)
                    val handler = (channelFuture as ChannelFuture).channel().pipeline().get(RpcClientHandler::class.java)
                    addHandler(handler)
                }
            }
        })
    }

    private fun addHandler(handler: RpcClientHandler) {
        connectedHandlers.add(handler)
        val remoteAddress = handler.channel!!.remoteAddress() as InetSocketAddress
        connectedServerNodes.put(remoteAddress, handler)
        signalAvailableHandler()
    }

    private fun signalAvailableHandler() {
        lock.lock()
        try {
            connected.signalAll()
        } finally {
            lock.unlock()
        }
    }

    @Throws(InterruptedException::class)
    private fun waitingForHandler(): Boolean {
        lock.lock()
        try {
            return connected.await(connectTimeoutMillis, TimeUnit.MILLISECONDS)
        } finally {
            lock.unlock()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun chooseHandler(): RpcClientHandler {
        var handlers = connectedHandlers.clone() as CopyOnWriteArrayList<RpcClientHandler>
        var size = handlers.size
        while (isRunning.get() && size <= 0) {
            try {
                val available = waitingForHandler()
                if (available) {
                    handlers = connectedHandlers.clone() as CopyOnWriteArrayList<RpcClientHandler>
                    size = handlers.size
                }
            } catch (ex: InterruptedException) {
                logger.error("Waiting for available node is interrupted!", ex)
                throw RuntimeException("Can't connect any servers!", ex)
            }

        }
        val index = (roundRobin.getAndAdd(1) + size) % size

        return handlers[index]
    }

    fun stop() {
        isRunning.set(false)
        connectedHandlers.forEach { handler -> handler.close() }
        signalAvailableHandler()
        threadPoolExecutor.shutdown()
        eventLoopGroup.shutdownGracefully()
    }

    companion object {
        private object InstanceHolder {
            internal val connectionInstance = ConnectionManager()
        }

        fun getInstance(): ConnectionManager = InstanceHolder.connectionInstance
    }
}
