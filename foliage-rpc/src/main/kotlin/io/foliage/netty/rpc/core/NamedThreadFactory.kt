package io.foliage.netty.rpc.core

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class NamedThreadFactory : ThreadFactory {

    private val threadPoolNum = AtomicInteger(1)
    private val threadNum = AtomicInteger(1)
    private val threadGroup: ThreadGroup
    private val prefix: String
    private val daemon: Boolean

    constructor() {
        this.prefix = "rpcserver-threadpool-" + threadPoolNum.andIncrement
        this.daemon = false
    }

    constructor(prefix: String) {
        this.prefix = prefix
        this.daemon = false
    }

    constructor(prefix: String, daemon: Boolean) {
        this.prefix = prefix + "-thread-"
        this.daemon = daemon
    }

    init {
        val security = System.getSecurityManager()
        this.threadGroup = if (security == null) Thread.currentThread().threadGroup else security.threadGroup
    }

    override fun newThread(r: Runnable): Thread {
        val t = Thread(threadGroup, r, prefix + threadNum.andIncrement, 0)
        t.isDaemon = daemon

        return t
    }
}