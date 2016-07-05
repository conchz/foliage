package org.lavenderx.foliage.sample;

import org.lavenderx.foliage.sample.config.ServerConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class RpcBootstrap {

    public static void main(String[] args) {
        final AbstractApplicationContext ctx =
                new AnnotationConfigApplicationContext(ServerConfig.class);

        Runtime.getRuntime().addShutdownHook(new Thread(ctx::close));
    }
}
