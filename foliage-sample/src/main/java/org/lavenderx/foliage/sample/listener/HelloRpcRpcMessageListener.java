package org.lavenderx.foliage.sample.listener;

import lombok.extern.slf4j.Slf4j;
import org.lavenderx.foliage.nettyrpc.annotation.RpcListenerContainer;
import org.lavenderx.foliage.nettyrpc.rpcservice.HelloRpcService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RpcListenerContainer(value = HelloRpcService.class)
public class HelloRpcRpcMessageListener implements HelloRpcService {

    @Override
    public String hello(String name) {
        return "Hello, " + name + "!";
    }
}
