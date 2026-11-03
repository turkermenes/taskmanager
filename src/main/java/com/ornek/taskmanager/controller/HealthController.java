package com.ornek.taskmanager.controller;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "time", LocalDateTime.now()
        );
    }

    @GetMapping("/hello/{name}")
    public String hello(@PathVariable String name, @RequestParam(defaultValue = "tr") String lang) {
        return lang.equals("tr") ? "Merhaba " + name + ", Task Manager'a hoş geldin!" : "Hello " + name + ", welcome to Task Manager!";
    }
}
