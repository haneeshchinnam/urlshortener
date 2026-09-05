package com.example.urlshortener.service;

import com.example.urlshortener.data.dto.Url;
import com.example.urlshortener.data.interfaces.URLShortenerInterface;
import com.example.urlshortener.repository.UrlRepository;
import com.example.urlshortener.util.ShortCodeGenerator;
import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class URLShortenerService implements URLShortenerInterface {

    private static final Logger log = LoggerFactory.getLogger(URLShortenerService.class);
    private static final String REDIS_KEY_PREFIX = "url:";

    private final ShortCodeGenerator shortCodeGenerator;
    private final UrlRepository urlRepository;
    private final RedisService redisService;
    private final Cache<String, UrlCacheEntry> localCache;
    private final Duration redisTtl;

    public URLShortenerService(
            ShortCodeGenerator shortCodeGenerator,
            UrlRepository urlRepository,
            RedisService redisService,
            Cache<String, UrlCacheEntry> localCache,
            @Value("${app.cache.redis.ttl:30m}") Duration redisTtl) {
        this.shortCodeGenerator = shortCodeGenerator;
        this.urlRepository = urlRepository;
        this.redisService = redisService;
        this.localCache = localCache;
        this.redisTtl = redisTtl;
    }

    @Override
    public String generateShortUrl(String url) {
        String shortUrl = shortCodeGenerator.generate();
        LocalDateTime now = LocalDateTime.now();
        urlRepository.save(Url.builder()
                .originalUrl(url)
                .createdAt(now)
                .expiresAt(now.plusHours(1))
                .shortCode(shortUrl)
                .build());

        // The database is the source of truth. The first read repopulates both
        // cache levels after this cache-aside invalidation.
        localCache.invalidate(shortUrl);
        deleteFromRedis(shortUrl);
        return shortUrl;
    }

    @Override
    public String getOriginalUrl(String shortUrl) {
        LocalDateTime now = LocalDateTime.now();

        UrlCacheEntry localEntry = localCache.getIfPresent(shortUrl);
        if (localEntry != null) {
            if (!localEntry.isExpired(now)) {
                return localEntry.originalUrl();
            }
            localCache.invalidate(shortUrl);
            deleteFromRedis(shortUrl);
            return null;
        }

        RedisService.RedisValue redisValue = getFromRedis(shortUrl);
        if (redisValue != null) {
            localCache.put(shortUrl,
                    new UrlCacheEntry(redisValue.value(), now.plus(redisValue.ttl())));
            return redisValue.value();
        }

        return urlRepository.findByShortCode(shortUrl)
                .filter(url -> !isExpired(url, now))
                .map(url -> {
                    cache(shortUrl, url, now);
                    return url.getOriginalUrl();
                })
                .orElse(null);
    }

    private void cache(String shortUrl, Url url, LocalDateTime now) {
        Duration ttl = redisTtl;
        if (url.getExpiresAt() != null) {
            Duration untilExpiration = Duration.between(now, url.getExpiresAt());
            if (untilExpiration.isNegative() || untilExpiration.isZero()) {
                return;
            }
            if (untilExpiration.compareTo(ttl) < 0) {
                ttl = untilExpiration;
            }
        }

        localCache.put(shortUrl, new UrlCacheEntry(url.getOriginalUrl(), url.getExpiresAt()));
        saveToRedis(shortUrl, url.getOriginalUrl(), ttl);
    }

    private boolean isExpired(Url url, LocalDateTime now) {
        return url.getExpiresAt() != null && !url.getExpiresAt().isAfter(now);
    }

    private RedisService.RedisValue getFromRedis(String shortUrl) {
        try {
            return redisService.getWithTtl(redisKey(shortUrl));
        } catch (DataAccessException exception) {
            log.warn("Redis read failed for short code {}; falling back to the database", shortUrl, exception);
            return null;
        }
    }

    private void saveToRedis(String shortUrl, String originalUrl, Duration ttl) {
        try {
            redisService.save(redisKey(shortUrl), originalUrl, ttl);
        } catch (DataAccessException exception) {
            log.warn("Redis write failed for short code {}; continuing with the local cache", shortUrl, exception);
        }
    }

    private void deleteFromRedis(String shortUrl) {
        try {
            redisService.delete(redisKey(shortUrl));
        } catch (DataAccessException exception) {
            log.warn("Redis eviction failed for short code {}", shortUrl, exception);
        }
    }

    private String redisKey(String shortUrl) {
        return REDIS_KEY_PREFIX + shortUrl;
    }
}
