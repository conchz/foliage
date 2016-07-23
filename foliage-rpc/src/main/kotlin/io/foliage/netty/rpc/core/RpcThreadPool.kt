package io.foliage.netty.rpc.core

import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class RpcThreadPool {

    companion object {
        private val name = "RpcThreadPool"

        fun getExecutor(threadNum: Int): Executor =
                ThreadPoolExecutor(threadNum, threadNum, 0, TimeUnit.SECONDS,
                        LinkedBlockingQueue<Runnable>(),
                        NamedThreadFactory(name, true),
                        AbortPolicyWithReport(name))
    }
}