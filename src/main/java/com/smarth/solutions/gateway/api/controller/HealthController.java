package com.smarth.solutions.gateway.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/healths")
public class HealthController {

    @GetMapping
    public String health() {
        return "OK";
    }
}