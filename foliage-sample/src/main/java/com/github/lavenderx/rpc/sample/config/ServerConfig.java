package com.github.lavenderx.rpc.sample.config;

import com.github.lavenderx.netty_rpc.config.RootConfig;
import com.github.lavenderx.netty_rpc.registry.ServiceRegistry;
import com.github.lavenderx.netty_rpc.server.RpcServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("com.github.lavenderx.rpc.sample.server")
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
