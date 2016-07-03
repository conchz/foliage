package org.lavenderx.foliage.api;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class ApiBootstrap {

    public static void main(String[] args) {
        final AbstractApplicationContext ctx =
                new AnnotationConfigApplicationContext();

        Runtime.getRuntime().addShutdownHook(new Thread(ctx::close));
    }
}
