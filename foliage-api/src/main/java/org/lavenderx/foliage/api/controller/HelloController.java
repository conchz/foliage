package org.lavenderx.foliage.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.lavenderx.foliage.nettyrpc.rpcservice.HelloRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@Slf4j
public class HelloController {

    @Autowired
    private HelloRpcService helloRpcService;

    @GetMapping(path = "/hello")
    public ResponseEntity<String> hello() {
        String result = helloRpcService.hello("World");
        log.info("Rpc call result: {}", result);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
