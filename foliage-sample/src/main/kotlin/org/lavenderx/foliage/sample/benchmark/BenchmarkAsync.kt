package org.lavenderx.foliage.sample.benchmark

import org.lavenderx.foliage.nettyrpc.client.RpcClient
import org.lavenderx.foliage.nettyrpc.client.RpcProxy
import org.lavenderx.foliage.nettyrpc.registry.ServiceDiscovery
import org.lavenderx.foliage.nettyrpc.rpcservice.HelloRpcService
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    val serviceDiscovery = ServiceDiscovery("127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183")
    val rpcClient = RpcClient(serviceDiscovery)

    val threadNum = 10
    val requestNum = 20
    val threads = arrayOfNulls<Thread>(threadNum)

    val startTime = System.currentTimeMillis()
    // Benchmark for async call
    for (i in 0..threadNum - 1) {
        threads[i] = Thread {
            for (j in 0..requestNum - 1) {
                try {
                    val client = RpcProxy.createAsync(HelloRpcService::class.java)
                    val helloFuture = client.call("hello", Integer.toString(j))
                    if (helloFuture.get(3000, TimeUnit.MILLISECONDS) != "Hello! " + j) {
                        println("error = " + helloFuture.get(3000, TimeUnit.MILLISECONDS)!!)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
        threads[i]?.start()
    }

    for (thread in threads) {
        thread?.join()
    }

    val timeCost = System.currentTimeMillis() - startTime
    val msg = String.format("Async call total-time-cost:%sms, req/s=%s",
            timeCost, (requestNum * threadNum).toDouble() / timeCost * 1000)
    println(msg)

    rpcClient.stop()
}
