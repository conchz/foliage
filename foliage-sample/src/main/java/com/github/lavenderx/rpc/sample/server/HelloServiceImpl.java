package com.github.lavenderx.rpc.sample.server;

import com.github.lavenderx.netty_rpc.server.RpcService;
import com.github.lavenderx.rpc.sample.client.HelloService;
import com.github.lavenderx.rpc.sample.client.Person;

@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        return "Hello! " + name;
    }

    @Override
    public String hello(Person person) {
        return "Hello! " + person.getFirstName() + " " + person.getLastName();
    }
}
