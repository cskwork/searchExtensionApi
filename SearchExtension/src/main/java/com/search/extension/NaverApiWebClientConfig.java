package com.search.extension;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class NaverApiWebClientConfig {

    @Value("${naver.api.base-url}")
    private String naverApiBaseUrl;

    @Value("${naver.api.client-id}")
    private String naverApiClientId;

    @Value("${naver.api.client-secret}")
    private String naverApiClientSecret;
    
    @Bean(name = "naverApiWebClient")
    public WebClient naverApiWebClient() {
        return WebClient.builder()
                .baseUrl(naverApiBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + naverApiClientSecret)
                .defaultHeader("X-Naver-Client-Id", naverApiClientId)
                .defaultHeader("X-Naver-Client-Secret", naverApiClientSecret)
                .build();
    }  
}
/*
# TEST RUN
curl  "https://openapi.naver.com/v1/search/blog.xml?query=%EB%A6%AC%EB%B7%B0&display=10&start=1&sort=sim" \
    -H "X-Naver-Client-Id: {애플리케이션 등록 시 발급받은 클라이언트 아이디 값}" \
    -H "X-Naver-Client-Secret: {애플리케이션 등록 시 발급받은 클라이언트 시크릿 값}" -v
*/