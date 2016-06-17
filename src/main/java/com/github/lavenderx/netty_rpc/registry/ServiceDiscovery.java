package com.github.lavenderx.netty_rpc.registry;

import com.github.lavenderx.netty_rpc.client.ConnectionManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class ServiceDiscovery {

    private final CountDownLatch latch = new CountDownLatch(1);
    private final String registryAddress;
    private final ZooKeeper zookeeper;

    private volatile List<String> dataList = new ArrayList<>();

    public ServiceDiscovery(String registryAddress) {
        this.registryAddress = registryAddress;
        this.zookeeper = connectServer();
        if (this.zookeeper != null) {
            watchNode(this.zookeeper);
        }
    }

    public String discover() {
        String data = null;
        int size = dataList.size();
        if (size > 0) {
            if (size == 1) {
                data = dataList.get(0);
                log.debug("using only data: {}", data);
            } else {
                data = dataList.get(ThreadLocalRandom.current().nextInt(size));
                log.debug("using random data: {}", data);
            }
        }
        return data;
    }

    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, Constants.ZK_SESSION_TIMEOUT, event -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    latch.countDown();
                }
            });
            latch.await();
        } catch (IOException | InterruptedException ex) {
            log.error("", ex);
        }
        return zk;
    }

    private void watchNode(final ZooKeeper zk) {
        try {
            List<String> nodeList = zk.getChildren(Constants.ZK_REGISTRY_PATH, event -> {
                if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                    watchNode(zk);
                }
            });
            List<String> dataList = new ArrayList<>();
            for (String node : nodeList) {
                byte[] bytes = zk.getData(Constants.ZK_REGISTRY_PATH + "/" + node, false, null);
                dataList.add(new String(bytes));
            }
            log.debug("node data: {}", dataList);
            this.dataList = dataList;

            log.debug("Service discovery triggered updating connected server node.");
            UpdateConnectedServer();
        } catch (KeeperException | InterruptedException ex) {
            log.error("", ex);
        }
    }

    private void UpdateConnectedServer() {
        ConnectionManager.getInstance().updateConnectedServer(this.dataList);
    }

    public void stop() {
        if (this.zookeeper != null) {
            try {
                this.zookeeper.close();
            } catch (InterruptedException ex) {
                log.error("", ex);
            }
        }
    }
}
