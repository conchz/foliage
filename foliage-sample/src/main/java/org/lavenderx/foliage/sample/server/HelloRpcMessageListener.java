package org.lavenderx.foliage.sample.server;

import org.lavenderx.foliage.nettyrpc.rpcservice.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HelloRpcMessageListener implements HelloService {

    @Override
    public String hello(String name) {
        return "Hello, " + name + "!";
    }
}
