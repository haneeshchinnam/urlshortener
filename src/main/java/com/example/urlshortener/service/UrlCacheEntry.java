package com.example.urlshortener.service;

import java.time.LocalDateTime;

public record UrlCacheEntry(String originalUrl, LocalDateTime expiresAt) {

    public boolean isExpired(LocalDateTime now) {
        return expiresAt != null && !expiresAt.isAfter(now);
    }
}
