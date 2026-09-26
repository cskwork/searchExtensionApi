package com.search.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.search.extension.apiSearch.adapter.persistence.PopularKeywordQueryRepository;
import com.search.extension.apiSearch.adapter.persistence.SearchKeywordHistoryJpaRepository;
import com.search.extension.apiSearch.application.exception.ApiRequestsFailedException;
import com.search.extension.apiSearch.application.port.KakaoBlogSearchService;
import com.search.extension.apiSearch.application.port.NaverBlogSearchService;
import com.search.extension.apiSearch.application.service.ApiBlogSearchServiceImpl;
import com.search.extension.apiSearch.domain.SearchKeywordHistory;
import com.search.extension.apiSearch.domain.model.ApiConstants;
import com.search.extension.apiSearch.domain.model.ErrorResponse;
import com.search.extension.config.AppConfig;

/**
 * 카카오 → 네이버 순서의 검색 오케스트레이션(Resilience4j + Try.recoverWith) 테스트
 * 공급자는 모두 목(mock)이며 외부 API 를 호출하지 않습니다.
 */
public class ApiBlogSearchServiceFallbackTest {
	private final Pageable firstPage = PageRequest.of(1, 10);

	private ApiBlogSearchServiceImpl service;
	private KakaoBlogSearchService kakaoApi;
	private NaverBlogSearchService naverApi;
	private SearchKeywordHistoryJpaRepository keywordRepository;

	@BeforeEach
	void setUp() {
		kakaoApi = mock(KakaoBlogSearchService.class);
		naverApi = mock(NaverBlogSearchService.class);
		keywordRepository = mock(SearchKeywordHistoryJpaRepository.class);
		service = new ApiBlogSearchServiceImpl();
		ReflectionTestUtils.setField(service, "kakaoApi", kakaoApi);
		ReflectionTestUtils.setField(service, "naverApi", naverApi);
		ReflectionTestUtils.setField(service, "keywordJpaRepository", keywordRepository);
		ReflectionTestUtils.setField(service, "popularKeywordQueryRepository", mock(PopularKeywordQueryRepository.class));
		// 실제 애플리케이션과 같은 서킷브레이커 설정
		ReflectionTestUtils.setField(service, "circuitBreakerRegistry", new AppConfig().circuitBreakerRegistry());
	}

	@Test
	void kakaoSuccessReturnsKakaoResultAndRecordsKakaoKeyword() {
		Map<String, Object> kakaoResult = Map.of("currentPage", 1);
		when(kakaoApi.getApiSearchResults("spring", "accuracy", firstPage)).thenReturn(kakaoResult);

		assertSame(kakaoResult, service.getApiSearchResults("spring", "accuracy", firstPage));
		assertEquals(ApiConstants.KAKAO_NAME, savedHistory().getApiSource());
		verifyNoInteractions(naverApi);
	}

	@Test
	void kakaoOutageFallsBackToNaverWithSameParameters() {
		Map<String, Object> naverResult = Map.of("currentPage", 2);
		Pageable secondPage = PageRequest.of(2, 20);
		when(kakaoApi.getApiSearchResults(anyString(), anyString(), any()))
			.thenThrow(new IllegalStateException("503 Service Unavailable"));
		when(naverApi.getApiSearchResults("spring", "recency", secondPage)).thenReturn(naverResult);

		assertSame(naverResult, service.getApiSearchResults("spring", "recency", secondPage));
		SearchKeywordHistory history = savedHistory();
		assertEquals(ApiConstants.NAVER_NAME, history.getApiSource());
		assertEquals("spring", history.getKeyword());
		assertEquals(1, history.getCount());
	}

	@Test
	void kakaoPageOutOfBoundsAlsoFallsBackToNaver() {
		Map<String, Object> naverResult = Map.of("currentPage", 1);
		when(kakaoApi.getApiSearchResults(anyString(), anyString(), any()))
			.thenThrow(new ApiRequestsFailedException(ErrorResponse.PAGE_OUT_OF_BOUNDS));
		when(naverApi.getApiSearchResults("spring", "accuracy", firstPage)).thenReturn(naverResult);

		assertSame(naverResult, service.getApiSearchResults("spring", "accuracy", firstPage));
	}

	@Test
	void bothProvidersDownReturnsApiCallFail() {
		when(kakaoApi.getApiSearchResults(anyString(), anyString(), any()))
			.thenThrow(new IllegalStateException("503 Service Unavailable"));
		when(naverApi.getApiSearchResults(anyString(), anyString(), any()))
			.thenThrow(new IllegalStateException("500 Internal Server Error"));

		ApiRequestsFailedException ex = assertThrows(ApiRequestsFailedException.class,
				() -> service.getApiSearchResults("spring", "accuracy", firstPage));
		assertEquals(ErrorResponse.API_CALL_FAIL, ex.getErrorResponse());
		verify(keywordRepository, never()).save(any());
	}

	@Test
	void bothProvidersOutOfBoundsReturnsPageOutOfBounds() {
		when(kakaoApi.getApiSearchResults(anyString(), anyString(), any()))
			.thenThrow(new ApiRequestsFailedException(ErrorResponse.PAGE_OUT_OF_BOUNDS));
		when(naverApi.getApiSearchResults(anyString(), anyString(), any()))
			.thenThrow(new ApiRequestsFailedException(ErrorResponse.PAGE_OUT_OF_BOUNDS));

		ApiRequestsFailedException ex = assertThrows(ApiRequestsFailedException.class,
				() -> service.getApiSearchResults("spring", "accuracy", PageRequest.of(50, 50)));
		assertEquals(ErrorResponse.PAGE_OUT_OF_BOUNDS, ex.getErrorResponse());
	}

	@Test
	void serviceStillValidatesBeforeCallingProviders() {
		ApiRequestsFailedException sort = assertThrows(ApiRequestsFailedException.class,
				() -> service.getApiSearchResults("spring", "popular", firstPage));
		assertEquals(ErrorResponse.INVALID_PARAMETER_SORT, sort.getErrorResponse());

		ApiRequestsFailedException page = assertThrows(ApiRequestsFailedException.class,
				() -> service.getApiSearchResults("spring", "accuracy", PageRequest.of(0, 10)));
		assertEquals(ErrorResponse.INVALID_PARAMETER_PAGE, page.getErrorResponse());

		verifyNoInteractions(kakaoApi, naverApi);
	}

    @Test void persistenceFailureDoesNotRetryProviderOrRecordCircuitFailure(){
        when(kakaoApi.getApiSearchResults(anyString(),anyString(),any())).thenReturn(Map.of("currentPage",1));
        when(keywordRepository.save(any())).thenThrow(new org.springframework.dao.DataAccessResourceFailureException("synthetic unavailable database"));
        ApiRequestsFailedException error=assertThrows(ApiRequestsFailedException.class,()->service.getApiSearchResults("spring","accuracy",firstPage));
        assertEquals(ErrorResponse.INTERNAL_SERVER_ERROR,error.getErrorResponse());verifyNoInteractions(naverApi);
    }
    @Test void emptyProviderResponseIsNotSavedAsSuccess(){
        when(kakaoApi.getApiSearchResults(anyString(),anyString(),any())).thenReturn(null);
        assertThrows(ApiRequestsFailedException.class,()->service.getApiSearchResults("spring","accuracy",firstPage));verify(keywordRepository,never()).save(any());
    }
	private SearchKeywordHistory savedHistory() {
		ArgumentCaptor<SearchKeywordHistory> captor = ArgumentCaptor.forClass(SearchKeywordHistory.class);
		verify(keywordRepository).save(captor.capture());
		return captor.getValue();
	}
}
