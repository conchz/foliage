package org.lavenderx.foliage.sample.config;

import org.lavenderx.foliage.nettyrpc.client.RpcClient;
import org.lavenderx.foliage.nettyrpc.config.RootConfig;
import org.lavenderx.foliage.nettyrpc.registry.ServiceDiscovery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("com.github.lavenderx.rpc.sample.client")
@Import(RootConfig.class)
public class ClientConfig {

    @Value("${registry.address}")
    private String registryAddress;

    @Value("${server.address}")
    private String serverAddress;

    @Bean
    public ServiceDiscovery serviceDiscovery() {
        return new ServiceDiscovery(registryAddress);
    }

    @Bean
    public RpcClient rpcClient() {
        return new RpcClient(serviceDiscovery());
    }
}
