package com.search.extension;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.search.extension.apiSearch.application.port.KakaoBlogSearchService;
import com.search.extension.apiSearch.application.port.NaverBlogSearchService;
import com.search.extension.apiSearch.application.service.KakaoBlogSearchServiceImpl;
import com.search.extension.apiSearch.application.service.NaverBlogSearchServiceImpl;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class AppConfig {
	/**
	 * Circuit Breaker - Resilience4j
	 * 문서 참고 : https://resilience4j.readme.io/docs/circuitbreaker
	 * @return
	 */
	@Bean 
	public CircuitBreakerRegistry circuitBreakerRegistry() {
		CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
				.failureRateThreshold(50)											// 임계값 보다 높으면 호출 차단 : CircuitBreaker transitions to open and starts short-circuiting calls.
			    .waitDurationInOpenState(Duration.ofMillis(1000))					// 반차단까직 대기하는 시간 : time that the CircuitBreaker should wait before transitioning from open to half-open.
			    .permittedNumberOfCallsInHalfOpenState(2)							// 반차단에서 호출 허용
			    .slidingWindowSize(2) 												// record the outcome of calls when the CircuitBreaker is closed
			    .recordExceptions(IOException.class, TimeoutException.class) 		//  exceptions that are recorded as a failure and thus increase the failure rate
				.build();

		CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(defaultConfig);

		return registry;
	}
	@Bean
	public KakaoBlogSearchService kakaoApi() {
		return new KakaoBlogSearchServiceImpl();
	}
	@Bean
	public NaverBlogSearchService naverApi() {
		return new NaverBlogSearchServiceImpl();
	}

}
