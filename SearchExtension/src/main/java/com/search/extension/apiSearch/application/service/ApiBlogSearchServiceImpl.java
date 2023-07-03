package com.search.extension.apiSearch.application.service;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.search.extension.apiSearch.adapter.persistence.PopularKeywordRepository;
import com.search.extension.apiSearch.application.exception.ApiInvalidParameterException;
import com.search.extension.apiSearch.application.exception.ApiRequestsFailedException;
import com.search.extension.apiSearch.application.port.ApiBlogSearchService;
import com.search.extension.apiSearch.application.port.KakaoBlogSearchService;
import com.search.extension.apiSearch.application.port.NaverBlogSearchService;
import com.search.extension.apiSearch.domain.PopularKeyword;
import com.search.extension.apiSearch.domain.model.ApiConstants;
import com.search.extension.apiSearch.domain.model.BlogSearchResultDTO;
import com.search.extension.apiSearch.domain.model.PopularKeywordDTO;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ApiBlogSearchServiceImpl implements ApiBlogSearchService {
	
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
	public BlogSearchResultDTO getApiSearchResults(String query, String sort, int page, int size) {
		boolean isValidParameter = isValidParameter(sort, size, page);
		log.info("isValidParameter : " + isValidParameter);

		CircuitBreaker firstCircuitBreaker = circuitBreakerRegistry.circuitBreaker("kakaoApi");
		CircuitBreaker secondCircuitBreaker = circuitBreakerRegistry.circuitBreaker("naverApi");
		CheckedFunction0<BlogSearchResultDTO> firstApiCall = CircuitBreaker
				.decorateCheckedSupplier(firstCircuitBreaker,
				() -> {
					log.info("Using "+ApiConstants.KAKAO_NAME+" API");
					BlogSearchResultDTO searchResult = kakaoApi.getApiSearchResults(query, sort, page, size);
					if (searchResult != null) {
						addPopularKeyword(query, 1, ApiConstants.KAKAO_NAME);
					}	
					return searchResult;
				});

		CheckedFunction0<BlogSearchResultDTO> secondApiCall = CircuitBreaker
				.decorateCheckedSupplier(secondCircuitBreaker, () -> {
					log.info("Using "+ApiConstants.NAVER_NAME+" API");
					BlogSearchResultDTO searchResult = naverApi.getApiSearchResults(query, sort, page, size);
					if (searchResult != null) {
						addPopularKeyword(query, 1, ApiConstants.NAVER_NAME);
					}
					return searchResult;
				});

		return Try.of(firstApiCall)
				.recoverWith(throwable -> Try.of(secondApiCall))
				// API 하단에 추가 
				// 예) .recoverWith(throwable -> Try.of(thirdApiCall))
				// ...
				.onFailure(e -> {
					log.error("Error message: " + e.getMessage());
					throw new ApiRequestsFailedException("API CALL FAIL");
				}).getOrElse(() -> {
					String callFailMessage= "ALL API CALLS FAILED";
					log.error(callFailMessage);
					throw new ApiRequestsFailedException(callFailMessage);
				});
	}
	
//	public CheckedFunction0<BlogSearchResultDTO> makeApiCallCircuitBreaker(String query, String sort, String page, String size){
//		CircuitBreaker firstCircuitBreaker = circuitBreakerRegistry.circuitBreaker("kakaoApi");
//		CircuitBreaker secondCircuitBreaker = circuitBreakerRegistry.circuitBreaker("naverApi");
//		CheckedFunction0<BlogSearchResultDTO> firstApiCall = CircuitBreaker
//			.decorateCheckedSupplier(firstCircuitBreaker,
//			() -> {
//				log.info("Using "+ApiConstants.KAKAO_NAME+" API");
//				BlogSearchResultDTO searchResult = kakaoApi.search(query, sort, page, size);
//				if (searchResult != null) {
//					addPopularKeyword(query, 1, ApiConstants.KAKAO_NAME);
//				}	
//				return searchResult;
//			});	
//	}

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

	@Override
	public PopularKeywordDTO getPopularKeyword() {
		// TODO Auto-generated method stub
		keywordRepository.findById("1"); // BY DATE?
		return null;
	}

}
