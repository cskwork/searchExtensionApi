package com.search.extension.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class KakaoApiWebClientConfig {

    @Value("${kakao.api.base-url}")
    private String kakaoApiBaseUrl;

    @Value("${kakao.api.key}")
    private String kakaoApiKey;
    
    @Bean(name = "kakaoApiWebClient")
    public WebClient kakaoApiWebClient() {
        return WebClient.builder()
                .baseUrl(kakaoApiBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoApiKey)
                .build();
    }
}
/*
# TEST RUN
curl -v -X GET "https://dapi.kakao.com/v2/search/blog" \
--data-urlencode "query=https://brunch.co.kr/@tourism 집짓기" \
-H "Authorization: KakaoAK ${REST_API_KEY}"
*/
