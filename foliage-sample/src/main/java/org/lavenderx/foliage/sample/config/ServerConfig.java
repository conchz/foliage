package org.lavenderx.foliage.sample.config;

import org.lavenderx.foliage.nettyrpc.config.RootConfig;
import org.lavenderx.foliage.nettyrpc.registry.ServiceRegistry;
import org.lavenderx.foliage.nettyrpc.server.RpcServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("org.lavenderx.foliage.sample.listener")
@Import(RootConfig.class)
public class ServerConfig {

    @Value("${registry.address}")
    private String registryAddress;

    @Value("${server.address}")
    private String serverAddress;

    @Bean
    public ServiceRegistry serviceRegistry() {
        return new ServiceRegistry(registryAddress);
    }

    @Bean
    public RpcServer rpcServer() {
        return new RpcServer(serverAddress, serviceRegistry());
    }
}
