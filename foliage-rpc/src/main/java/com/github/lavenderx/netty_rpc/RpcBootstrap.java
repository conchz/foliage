package com.github.lavenderx.netty_rpc;

import com.github.lavenderx.netty_rpc.config.RootConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class RpcBootstrap {

    public static void main(String[] args) {
        AbstractApplicationContext ctx = new AnnotationConfigApplicationContext(RootConfig.class);

        Runtime.getRuntime().addShutdownHook(new Thread(ctx::close));
    }
}
