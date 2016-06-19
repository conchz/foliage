package com.github.lavenderx.rpc.sample.benchmark;

import com.github.lavenderx.netty_rpc.client.RpcClient;
import com.github.lavenderx.netty_rpc.client.RpcFuture;
import com.github.lavenderx.netty_rpc.client.RpcProxy;
import com.github.lavenderx.netty_rpc.registry.ServiceDiscovery;
import com.github.lavenderx.rpc.sample.client.HelloService;

import java.util.concurrent.TimeUnit;

public class BenchmarkAsync {

    public static void main(String[] args) throws InterruptedException {
        ServiceDiscovery serviceDiscovery = new ServiceDiscovery("127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183");
        RpcClient rpcClient = new RpcClient(serviceDiscovery);

        int threadNum = 10;
        int requestNum = 20;
        Thread[] threads = new Thread[threadNum];

        long startTime = System.currentTimeMillis();
        // Benchmark for async call
        for (int i = 0; i < threadNum; ++i) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < requestNum; j++) {
                    try {
                        RpcProxy<HelloService> client = rpcClient.createAsync(HelloService.class);
                        RpcFuture helloFuture = client.call("hello", Integer.toString(j));
                        String result = (String) helloFuture.get(3000, TimeUnit.MILLISECONDS);
//                        System.out.println(result);
                        if (!result.equals("Hello! " + j)) {
                            System.out.println("error = " + result);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        long timeCost = (System.currentTimeMillis() - startTime);
        String msg = String.format("Async call total-time-cost:%sms, req/s=%s",
                timeCost, ((double) (requestNum * threadNum)) / timeCost * 1000);
        System.out.println(msg);

        rpcClient.stop();

    }
}
