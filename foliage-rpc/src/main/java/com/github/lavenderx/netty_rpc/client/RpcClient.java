package com.github.lavenderx.netty_rpc.client;

import com.github.lavenderx.netty_rpc.registry.ServiceDiscovery;

import java.lang.reflect.Proxy;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcClient {

    private static ThreadPoolExecutor threadPoolExecutor =
            new ThreadPoolExecutor(16, 16, 10, TimeUnit.MINUTES, new ArrayBlockingQueue<>(65536));

    private String serverAddress;
    private ServiceDiscovery serviceDiscovery;

    public RpcClient(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new RpcProxy<>(interfaceClass)
        );
    }

    public static <T> RpcProxy<T> createAsync(Class<T> interfaceClass) {
        return new RpcProxy<>(interfaceClass);
    }

    public static void submit(Runnable task) {
        threadPoolExecutor.submit(task);
    }

    public void stop() {
        threadPoolExecutor.shutdown();
        serviceDiscovery.stop();
        ConnectionManager.getInstance().stop();
    }
}

