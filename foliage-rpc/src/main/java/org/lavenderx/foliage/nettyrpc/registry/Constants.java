package org.lavenderx.foliage.nettyrpc.registry;

public interface Constants {

    int ZK_SESSION_TIMEOUT = 5_000;

    String ZK_REGISTRY_PATH = "/registry";
    String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";
}
