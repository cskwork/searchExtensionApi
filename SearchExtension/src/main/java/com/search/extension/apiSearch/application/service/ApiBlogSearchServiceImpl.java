package com.search.extension.apiSearch.application.service;

import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.search.extension.apiSearch.adapter.persistence.PopularKeywordRepository;
import com.search.extension.apiSearch.application.exception.ApiInvalidParameterException;
import com.search.extension.apiSearch.application.exception.ApiRequestsFailedException;
import com.search.extension.apiSearch.application.port.ApiBlogSearchService;
import com.search.extension.apiSearch.application.port.KakaoBlogSearchService;
import com.search.extension.apiSearch.application.port.NaverBlogSearchService;
import com.search.extension.apiSearch.domain.PopularKeyword;
import com.search.extension.apiSearch.domain.model.BlogSearchResultDTO;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;

public class ApiBlogSearchServiceImpl implements ApiBlogSearchService {
	private static final Logger log = LogManager.getLogger(ApiBlogSearchServiceImpl.class);
	@Autowired
	private PopularKeywordRepository keywordRepository;

	@Autowired
	@Qualifier("kakaoApi")
	private KakaoBlogSearchService kakaoApi;

	@Autowired
	@Qualifier("naverApi")
	private NaverBlogSearchService naverApi;

	@Autowired
	private CircuitBreakerRegistry circuitBreakerRegistry;

	public static boolean isValidParameter(String sort, int size, int page) {
		if (!(sort.equals("accuracy") || sort.equals("recency"))) {
			throw new ApiInvalidParameterException("Sort Paramter Invalid");
		}
		if (size > 999999 || page > 99999) {
			throw new ApiInvalidParameterException("SIZE OUT OF BOUNDS");
		}
		return true;
	};

	@Override
	public BlogSearchResultDTO search(String query, String sort, int page, int size) {
		boolean isValidParameter = isValidParameter(sort, size, page);
		log.info("isValidParameter : " + isValidParameter);

		CircuitBreaker firstCircuitBreaker = circuitBreakerRegistry.circuitBreaker("kakaoApi");
		CircuitBreaker secondCircuitBreaker = circuitBreakerRegistry.circuitBreaker("naverApi");
		CheckedFunction0<BlogSearchResultDTO> firstApiCall = CircuitBreaker
				.decorateCheckedSupplier(firstCircuitBreaker,
				() -> {
					log.info("Using Kakao API");
					BlogSearchResultDTO searchResult = kakaoApi.search(query, sort, page, size);
					// @TODO INSERT POPULAR KEYWORD
					if (searchResult != null) {

					}
					//addPopularKeyword(query, 1, "kakao");
					return searchResult;
				});

		CheckedFunction0<BlogSearchResultDTO> secondApiCall = CircuitBreaker
				.decorateCheckedSupplier(secondCircuitBreaker, () -> {
					log.info("Using Naver API");
					BlogSearchResultDTO searchResult = naverApi.search(query, sort, page, size);
					// @TODO INSERT POPULAR KEYWORD
					if (!searchResult.equals("null")) {

					}
					//addPopularKeyword(query, 1, "naver");
					return searchResult;
				});

		return Try.of(firstApiCall).recoverWith(throwable -> Try.of(secondApiCall))
				// ADD ADDITIONAL API HERE
				// ...
				.onFailure(e -> {
					log.error("Error message: " + e.getMessage());
					// throw new ApiRequestsFailedException("API CALL FAIL");
				}).getOrElse(() -> {
					// return null;
					log.error("All API calls failed");
					throw new ApiRequestsFailedException("ALL API CALLS FAILED");
					// return null; // or return an appropriate default value
				});
	}

	public PopularKeyword addPopularKeyword(String keyword, Integer count, String apiSource) {
		PopularKeyword popularKeyword = new PopularKeyword();
		popularKeyword.setKeyword(keyword);
		popularKeyword.setCount(count);
		popularKeyword.setApiSource(apiSource);

		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		popularKeyword.setCreatedTime(currentTime);
		popularKeyword.setUpdatedTime(currentTime);

		return keywordRepository.save(popularKeyword);
	}

}
