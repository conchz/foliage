package org.lavenderx.foliage.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@Slf4j
public class HelloController {

    @GetMapping("/hello")
    public void hello() {
        log.info("Hello.........");
    }
}
