package com.github.lavenderx.rpc.sample.server;

import com.github.lavenderx.rpc.sample.config.ServerConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class RpcBootstrap {

    public static void main(String[] args) {
        final AbstractApplicationContext ctx =
                new AnnotationConfigApplicationContext(ServerConfig.class);

        Runtime.getRuntime().addShutdownHook(new Thread(ctx::close));
    }
}
