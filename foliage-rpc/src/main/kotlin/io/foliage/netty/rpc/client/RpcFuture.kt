package io.foliage.netty.rpc.client

import io.foliage.utils.loggerFor
import io.foliage.netty.rpc.protocol.RpcRequest
import io.foliage.netty.rpc.protocol.RpcResponse
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.AbstractQueuedSynchronizer
import java.util.concurrent.locks.ReentrantLock

class RpcFuture : Future<Any> {

    private val logger = loggerFor<RpcFuture>()

    private val sync: Sync
    private val request: RpcRequest
    private val startTime: Long
    private val responseTimeThreshold: Long = 5000

    private val pendingCallbacks = ArrayList<AsyncRpcCallback>()
    private val lock = ReentrantLock()

    private var response: RpcResponse? = null

    constructor(request: RpcRequest) {
        this.sync = Sync()
        this.request = request
        this.startTime = System.currentTimeMillis()
    }

    override fun isDone(): Boolean {
        return sync.isDone
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    override fun get(): Any? {
        sync.acquire(-1)
        if (response != null) {
            return response!!.result
        } else {
            return null
        }
    }

    @Throws(InterruptedException::class, ExecutionException::class, TimeoutException::class)
    override fun get(timeout: Long, unit: TimeUnit): Any? {
        val success = sync.tryAcquireNanos(-1, unit.toNanos(timeout))
        if (success) {
            if (response != null) {
                return response!!.result
            } else {
                return null
            }
        } else {
            throw RuntimeException("Timeout exception. Request id: " + request.requestId
                    + ". Request class name: " + request.className
                    + ". Request method: " + request.methodName)
        }
    }

    override fun isCancelled(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        throw UnsupportedOperationException()
    }

    fun done(response: RpcResponse) {
        this.response = response
        sync.release(1)
        invokeCallbacks()

        // Threshold
        val responseTime = System.currentTimeMillis() - startTime
        if (responseTime > responseTimeThreshold) {
            logger.warn("Service response time is too slow. Request id = {}. Response Time = {}ms",
                    response.requestId, responseTime)
        }
    }

    private fun invokeCallbacks() {
        lock.lock()
        try {
            pendingCallbacks.forEach { cb -> this.runCallback(cb) }
        } finally {
            lock.unlock()
        }
    }

    fun addCallback(callback: AsyncRpcCallback): RpcFuture {
        lock.lock()
        try {
            if (isDone) {
                runCallback(callback)
            } else {
                pendingCallbacks.add(callback)
            }
        } finally {
            lock.unlock()
        }

        return this
    }

    private fun runCallback(callback: AsyncRpcCallback) {
        val res = response
        RpcProxy.submitTaskToProxyPool(Runnable {
            if (!res!!.isError) {
                callback.success(res.result!!)
            } else {
                callback.fail(RuntimeException("Response error", Throwable(res.error)))
            }
        })
    }

    internal class Sync : AbstractQueuedSynchronizer() {

        // Future status
        private val done = 1
        private val pending = 0

        override fun tryAcquire(acquires: Int): Boolean {
            return state == done
        }

        override fun tryRelease(releases: Int): Boolean {
            if (state == pending) {
                if (compareAndSetState(pending, done)) {
                    return true
                }
            }
            return false
        }

        val isDone: Boolean
            get() {
                state
                return state == done
            }

        companion object {
            private val serialVersionUID = -3234409599877003143L
        }
    }
}