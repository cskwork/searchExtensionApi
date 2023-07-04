package com.search.extension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Qualifier;

import com.search.extension.apiSearch.adapter.persistence.SearchKeywordHistoryJpaRepository;
import com.search.extension.apiSearch.application.exception.ApiRequestsFailedException;
import com.search.extension.apiSearch.application.port.KakaoBlogSearchService;
import com.search.extension.apiSearch.application.port.NaverBlogSearchService;
import com.search.extension.apiSearch.application.service.ApiBlogSearchServiceImpl;
import com.search.extension.apiSearch.application.utils.ExceptionHandlerUtil;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

public class ApiBlogSearchServiceImplTest {

	@InjectMocks
	private ApiBlogSearchServiceImpl service;

	@Mock
	private SearchKeywordHistoryJpaRepository keywordRepository;

	@Mock
	@Qualifier("kakaoApi")
	private KakaoBlogSearchService kakaoApi;

	@Mock
	@Qualifier("naverApi")
	private NaverBlogSearchService naverApi;

	@Mock
	private CircuitBreakerRegistry circuitBreakerRegistry;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testIsValidParameter() {
		assertThrows(ApiRequestsFailedException.class,
				() -> ExceptionHandlerUtil.isValidParameter("invalid_sort", 10, 2));

		assertThrows(ApiRequestsFailedException.class,
				() -> ExceptionHandlerUtil.isValidParameter("accuracy", 1000000, 2));

		assertThrows(ApiRequestsFailedException.class,
				() -> ExceptionHandlerUtil.isValidParameter("accuracy", 10, 100000));

		assertDoesNotThrow(() -> ExceptionHandlerUtil.isValidParameter("accuracy", 1, 5));
	}
}
