package com.example.urlshortener.controller;

import com.example.urlshortener.data.dto.URLShortenerRequest;
import com.example.urlshortener.data.dto.URLShortenerResponse;
import com.example.urlshortener.data.interfaces.URLShortenerInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("")
public class UrlShortenerController {

    @Autowired
    private URLShortenerInterface urlShortenerInterface;

    @PostMapping("/v1/urls")
    public ResponseEntity<URLShortenerResponse> createShortUrl(@RequestBody URLShortenerRequest request) {
        // Logic to create a short URL would go here
        String shortUrl = urlShortenerInterface.generateShortUrl(request.getUrl()); // Placeholder for the generated short URL
        URLShortenerResponse response = new URLShortenerResponse(shortUrl);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable(name = "shortUrl") String shortUrl) {
        // Logic to retrieve the original URL from the short URL would go here
        String originalUrl = urlShortenerInterface.getOriginalUrl(shortUrl); // Placeholder for the original URL
        return ResponseEntity.status(302).header("Location", originalUrl).build();
    }
}
