package org.lavenderx.foliage.api.controller

import io.foliage.utils.loggerFor
import io.foliage.netty.rpc.rpcservice.HelloRpcService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
class HelloController @Autowired constructor(val helloRpcService: HelloRpcService) {

    private val logger = loggerFor<HelloController>()

    @GetMapping("/hello")
    fun hello(): ResponseEntity<String> {
        val result = helloRpcService.hello("World")
        logger.info("Rpc call result: {}", result)
        return ResponseEntity(result, HttpStatus.OK)
    }
}