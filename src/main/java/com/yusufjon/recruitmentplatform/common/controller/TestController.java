package com.yusufjon.recruitmentplatform.common.controller;

/**
 * Provides a simple protected endpoint that can be used to confirm authenticated requests reach
 * the application.
 */

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test/hello")
    public String hello() {
        return "Hello, authenticated user!";
    }
}