package io.foliage.api.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.PropertySource
import org.springframework.core.annotation.Order

@Order(0)
@Configuration
@ComponentScan("org.lavenderx.foliage.api.service")
@PropertySource("classpath:api.properties")
@Import(RpcCallableConfig::class)
open class ApiConfig {
}