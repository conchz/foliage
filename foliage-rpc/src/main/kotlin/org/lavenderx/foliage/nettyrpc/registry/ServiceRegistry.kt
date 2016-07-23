package org.lavenderx.foliage.nettyrpc.registry

import org.apache.zookeeper.CreateMode
import org.apache.zookeeper.Watcher
import org.apache.zookeeper.ZooDefs
import org.apache.zookeeper.ZooKeeper
import org.lavenderx.foliage.nettyrpc.utils.loggerFor
import java.util.concurrent.CountDownLatch

class ServiceRegistry(private val registryAddress: String) {

    private val logger = loggerFor<ServiceRegistry>()
    private val latch = CountDownLatch(1)

    fun register(data: String?) {
        if (data != null) {
            val zk = connectServer()
            if (zk != null) {
                AddRootNode(zk) // Add root node if not exist
                createNode(zk, data)
            }
        }
    }

    private fun connectServer(): ZooKeeper? {
        var zk: ZooKeeper? = null
        try {
            zk = ZooKeeper(registryAddress, Constants.ZK_SESSION_TIMEOUT, Watcher { event ->
                if (event.state == org.apache.zookeeper.Watcher.Event.KeeperState.SyncConnected) {
                    latch.countDown()
                }
            })
            latch.await()
        } catch (e: Exception) {
            logger.error("", e)
        }

        return zk
    }

    private fun AddRootNode(zk: ZooKeeper) {
        try {
            val s = zk.exists(Constants.ZK_REGISTRY_PATH, false)
            if (s == null) {
                zk.create(
                        Constants.ZK_REGISTRY_PATH,
                        ByteArray(0),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT)
            }
        } catch (e: Exception) {
            logger.error("", e)
        }

    }

    private fun createNode(zk: ZooKeeper, data: String) {
        try {
            val bytes = data.toByteArray()
            val path = zk.create(
                    Constants.ZK_DATA_PATH,
                    bytes,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL)
            logger.debug("create zookeeper node ({} => {})", path, data)
        } catch (e: Exception) {
            logger.error("", e)
        }

    }
}