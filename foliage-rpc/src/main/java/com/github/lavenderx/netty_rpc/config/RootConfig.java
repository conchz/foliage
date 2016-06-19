package com.github.lavenderx.netty_rpc.config;

import com.github.lavenderx.netty_rpc.client.RpcClient;
import com.github.lavenderx.netty_rpc.registry.ServiceDiscovery;
import com.github.lavenderx.netty_rpc.registry.ServiceRegistry;
import com.github.lavenderx.netty_rpc.server.RpcServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.Order;

@Order(0)
@Configuration
@PropertySource("classpath:rpc.properties")
public class RootConfig {

    @Value("${registry.address}")
    private String registryAddress;

    @Value("${server.address}")
    private String serverAddress;

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public ServiceDiscovery serviceDiscovery() {
        return new ServiceDiscovery(registryAddress);
    }

    @Bean
    public RpcClient rpcClient() {
        return new RpcClient(serviceDiscovery());
    }

    @Bean
    public ServiceRegistry serviceRegistry() {
        return new ServiceRegistry(registryAddress);
    }

    @Bean
    public RpcServer rpcServer() {
        return new RpcServer(serverAddress, serviceRegistry());
    }
}
