package io.foliage.netty.rpc.core

import org.lavenderx.foliage.nettyrpc.utils.loggerFor
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ThreadPoolExecutor

class AbortPolicyWithReport(val threadName: String) : ThreadPoolExecutor.AbortPolicy() {

    private val logger = loggerFor<AbortPolicyWithReport>()

    override fun rejectedExecution(r: Runnable, e: ThreadPoolExecutor) {
        val msg = String.format("RpcServer["
                + " Thread Name: %s, Pool Size: %d (active: %d, core: %d, max: %d, largest: %d), Task: %d (completed: %d),"
                + " Executor status: (isShutdown: %s, isTerminated: %s, isTerminating: %s)]",
                threadName, e.poolSize, e.activeCount, e.corePoolSize, e.maximumPoolSize, e.largestPoolSize,
                e.taskCount, e.completedTaskCount, e.isShutdown, e.isTerminated, e.isTerminating)
        logger.error(msg)

        throw RejectedExecutionException(msg)
    }
}