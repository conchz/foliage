package io.foliage.netty.rpc.registry;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ServiceRegistry {

    private final CountDownLatch latch = new CountDownLatch(1);
    private final String registryAddress;

    public ServiceRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void register(String data) {
        if (data != null) {
            ZooKeeper zk = connectServer();
            if (zk != null) {
                AddRootNode(zk); // Add root node if not exist
                createNode(zk, data);
            }
        }
    }

    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, Constants.ZK_SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (IOException | InterruptedException ex) {
            log.error("", ex);
        }
        return zk;
    }

    private void AddRootNode(ZooKeeper zk) {
        try {
            Stat s = zk.exists(Constants.ZK_REGISTRY_PATH, false);
            if (s == null) {
                zk.create(
                        Constants.ZK_REGISTRY_PATH,
                        new byte[0],
                        ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT
                );
            }
        } catch (KeeperException | InterruptedException ex) {
            log.error("", ex);
        }
    }

    private void createNode(ZooKeeper zk, String data) {
        try {
            byte[] bytes = data.getBytes();
            String path = zk.create(
                    Constants.ZK_DATA_PATH,
                    bytes,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL
            );
            log.debug("create zookeeper node ({} => {})", path, data);
        } catch (KeeperException | InterruptedException ex) {
            log.error("", ex);
        }
    }
}
