package com.search.extension;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.search.extension.apiSearch.application.port.ApiBlogSearchService;
import com.search.extension.apiSearch.application.port.KakaoBlogSearchService;
import com.search.extension.apiSearch.application.port.NaverBlogSearchService;
import com.search.extension.apiSearch.application.service.ApiBlogSearchServiceImpl;
import com.search.extension.apiSearch.application.service.KakaoBlogSearchServiceImpl;
import com.search.extension.apiSearch.application.service.NaverBlogSearchServiceImpl;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

@Configuration
public class AppConfig {

	@Bean
	public ApiBlogSearchService apiSearchService() {
		return new ApiBlogSearchServiceImpl();
	}

	@Bean
	public KakaoBlogSearchService kakaoApi() {
		return new KakaoBlogSearchServiceImpl();
	}

	@Bean
	public NaverBlogSearchService naverApi() {
		return new NaverBlogSearchServiceImpl();
	}

	@Bean
	public CircuitBreakerRegistry circuitBreakerRegistry() {
		CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
				.failureRateThreshold(50)
				.waitDurationInOpenState(Duration.ofMillis(1000))
				.ringBufferSizeInHalfOpenState(2)
				.ringBufferSizeInClosedState(2).build();

		CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(defaultConfig);

		return registry;
	}

}
