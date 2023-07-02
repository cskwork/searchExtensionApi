package com.search.extension.apiSearch.application.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.search.extension.apiSearch.application.port.ApiBlogSearchService;
import com.search.extension.apiSearch.application.port.KakaoBlogSearchService;
import com.search.extension.apiSearch.application.port.NaverBlogSearchService;
import com.search.extension.apiSearch.domain.model.BlogSearchResultDTO;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;

public class ApiBlogSearchServiceImpl implements ApiBlogSearchService {
    private static final Logger log = LogManager.getLogger(ApiBlogSearchServiceImpl.class);
    
    @Autowired
	@Qualifier("kakaoApi")
	private KakaoBlogSearchService kakaoApi;

	@Autowired
	@Qualifier("naverApi")
	private NaverBlogSearchService naverApi;

	@Autowired
	private CircuitBreakerRegistry circuitBreakerRegistry;

	@Override
	public BlogSearchResultDTO search(String query, String sort, int page, int size) {
	    CircuitBreaker firstCircuitBreaker = circuitBreakerRegistry.circuitBreaker("kakaoApi");
	    CircuitBreaker secondCircuitBreaker = circuitBreakerRegistry.circuitBreaker("naverApi");

	    CheckedFunction0<BlogSearchResultDTO> firstApiCall = CircuitBreaker
	            .decorateCheckedSupplier(firstCircuitBreaker,
	                    () -> kakaoApi.search(query, sort, page, size));

	    CheckedFunction0<BlogSearchResultDTO> secondApiCall = CircuitBreaker
	            .decorateCheckedSupplier(secondCircuitBreaker,
	                    () -> naverApi.search(query, sort, page, size));

	    // You can remove the try-catch block here
	    return Try.of(firstApiCall)
	            .recoverWith(throwable -> Try.of(secondApiCall))
	            // ADD ADDITIONAL API HERE
	            // ...
 
	            .onFailure(e -> {
	                e.printStackTrace();
	                log.error("Error message: " + e.getMessage());
	                // Handle the error or rethrow as necessary
	            })
	            .getOrElse(() -> {
	                // Provide a default value or create an error handling mechanism to return an appropriate response
	                log.error("All API calls failed");
	                return null; // or return an appropriate default value
	            });
	}


}
