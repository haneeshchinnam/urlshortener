package com.example.urlshortener.config;

import com.example.urlshortener.service.UrlCacheEntry;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, UrlCacheEntry> urlLocalCache(
            @Value("${app.cache.caffeine.maximum-size:10000}") long maximumSize,
            @Value("${app.cache.caffeine.ttl:5m}") Duration ttl) {
        return Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(ttl)
                .recordStats()
                .build();
    }
}
