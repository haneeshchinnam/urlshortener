package com.example.urlshortener.data.interfaces;

public interface URLShortenerInterface {

    String generateShortUrl(String url);

    String getOriginalUrl(String shortUrl);

}
