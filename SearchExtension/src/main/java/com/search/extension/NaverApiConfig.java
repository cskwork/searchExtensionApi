package com.search.extension;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@Configuration
public class NaverApiConfig {

    @Value("${naver.api.base-url}")
    private String naverApiBaseUrl;

    @Value("${naver.api.client-id}")
    private String naverApiClientId;

    @Value("${naver.api.client-secret}")
    private String naverApiClientSecret;

    @Bean(name = "naverApiRestTemplate")
    public RestTemplate naverApiRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .rootUri(naverApiBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + naverApiClientSecret)
                .defaultHeader("X-Naver-Client-Id", naverApiClientId)
                .defaultHeader("X-Naver-Client-Secret", naverApiClientSecret)
                .build();
    }
}