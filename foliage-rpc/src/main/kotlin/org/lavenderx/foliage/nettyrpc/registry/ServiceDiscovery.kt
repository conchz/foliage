package org.lavenderx.foliage.nettyrpc.registry

import org.apache.zookeeper.KeeperException
import org.apache.zookeeper.ZooKeeper
import org.lavenderx.foliage.nettyrpc.client.ConnectionManager
import org.lavenderx.foliage.nettyrpc.logging.loggerFor
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ThreadLocalRandom

class ServiceDiscovery(private val registryAddress: String) {

    private val logger = loggerFor<ServiceDiscovery>()
    private val latch = CountDownLatch(1)
    private val zookeeper: ZooKeeper?
    @Volatile private var dataList: List<String> = ArrayList()

    init {
        this.zookeeper = connectServer()
        if (this.zookeeper != null) {
            watchNode(this.zookeeper)
        }
    }


    fun discover(): String {
        var data: String? = null
        val size = dataList.size
        if (size > 0) {
            if (size == 1) {
                data = dataList[0]
                logger.debug("using only data: {}", data)
            } else {
                data = dataList[ThreadLocalRandom.current().nextInt(size)]
                logger.debug("using random data: {}", data)
            }
        }
        return data!!
    }

    private fun connectServer(): ZooKeeper? {
        var zk: ZooKeeper? = null
        try {
            zk = ZooKeeper(registryAddress, Constants.ZK_SESSION_TIMEOUT) { event ->
                if (event.state == org.apache.zookeeper.Watcher.Event.KeeperState.SyncConnected) {
                    latch.countDown()
                }
            }
            latch.await()
        } catch (e: Exception) {
            logger.error("", e)
        }

        return zk
    }

    private fun watchNode(zk: ZooKeeper) {
        try {
            val nodeList = zk.getChildren(Constants.ZK_REGISTRY_PATH) { event ->
                if (event.type == org.apache.zookeeper.Watcher.Event.EventType.NodeChildrenChanged) {
                    watchNode(zk)
                }
            }
            val dataList = ArrayList<String>()
            for (node in nodeList) {
                val bytes = zk.getData(Constants.ZK_REGISTRY_PATH + "/" + node, false, null)
                dataList.add(String(bytes))
            }
            logger.debug("node data: {}", dataList)
            this.dataList = dataList

            logger.debug("Service discovery triggered updating connected server node.")
            UpdateConnectedServer()
        } catch (e: KeeperException) {
            logger.error("", e)
        }

    }

    private fun UpdateConnectedServer() {
        ConnectionManager.getInstance().updateConnectedServer(this.dataList)
    }

    fun stop() {
        try {
            this.zookeeper?.close()
        } catch (e: InterruptedException) {
            logger.error("", e)
        }
    }
}