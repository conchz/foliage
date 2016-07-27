package io.foliage.netty.rpc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.Order;

@Order(0)
@Configuration
@ComponentScan("io.foliage.netty.rpc")
@PropertySource("classpath:rpc.properties")
public class RpcConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer defaultPropertyConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
