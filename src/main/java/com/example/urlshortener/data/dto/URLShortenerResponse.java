package com.example.urlshortener.data.dto;

import lombok.*;

@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor
@Setter
public class URLShortenerResponse {
    private String shortUrl;
}
