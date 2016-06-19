package com.github.lavenderx.rpc.sample.config;

import com.github.lavenderx.netty_rpc.config.RootConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("com.github.lavenderx.rpc.sample.client")
@Import(RootConfig.class)
public class ClientConfig {

}
