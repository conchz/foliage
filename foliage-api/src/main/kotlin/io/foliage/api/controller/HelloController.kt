package io.foliage.api.controller

import io.foliage.utils.loggerFor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
class HelloController /*@Autowired constructor*/() {

    private val logger = loggerFor<HelloController>()

    @GetMapping("/hello")
    fun hello(): ResponseEntity<String> {
        val result = "Hello World!"
        logger.info("Rpc call result: {}", result)
        return ResponseEntity(result, HttpStatus.OK)
    }
}