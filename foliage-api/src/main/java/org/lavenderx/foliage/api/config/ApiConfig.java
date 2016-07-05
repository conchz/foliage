package org.lavenderx.foliage.api.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;

@Order(1)
@Configuration
@ComponentScan("org.lavenderx.foliage.api.service")
@PropertySource("classpath:api.properties")
@Import(RpcCallableConfig.class)
public class ApiConfig {

}
