package com.example.urlshortener.controller;

import com.example.urlshortener.service.RedisService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/redis")
public class RedisController {

    private final RedisService redisService;

    public RedisController(RedisService redisService) {
        this.redisService = redisService;
    }

    @PostMapping("/{key}")
    public String save(
            @PathVariable String key,
            @RequestParam String value) {

        redisService.save(key, value);
        return "Saved";
    }

    @GetMapping("/{key}")
    public String get(@PathVariable String key) {
        return redisService.get(key);
    }

    @DeleteMapping("/{key}")
    public String delete(@PathVariable String key) {
        redisService.delete(key);
        return "Deleted";
    }
}
