package com.example.urlshortener.service;

import com.example.urlshortener.data.dto.Url;
import com.example.urlshortener.data.interfaces.URLShortenerInterface;
import com.example.urlshortener.repository.UrlRepository;
import com.example.urlshortener.util.ShortCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;

import static java.time.temporal.ChronoUnit.HOURS;

@Service
public class URLShortenerService implements URLShortenerInterface {

    @Autowired
    private ShortCodeGenerator shortCodeGenerator;

    @Autowired
    UrlRepository urlRepository;

    @Override
    public String generateShortUrl(String url) {
        String shortUrl = shortCodeGenerator.generate();
        urlRepository.save(Url.builder().originalUrl(url).createdAt(LocalDateTime.now()).expiresAt(LocalDateTime.now().plusHours(1)).shortCode(shortUrl).build());
        return shortUrl;
    }

    @Override
    public String getOriginalUrl(String shortUrl) {
        return urlRepository.findByShortCode(shortUrl)
                .map(Url::getOriginalUrl)
                .orElse(null);
    }
}
