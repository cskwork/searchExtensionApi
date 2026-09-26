package com.search.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.search.extension.apiSearch.application.exception.ApiRequestsFailedException;
import com.search.extension.apiSearch.application.service.KakaoBlogSearchServiceImpl;
import com.search.extension.apiSearch.application.service.NaverBlogSearchServiceImpl;
import com.search.extension.apiSearch.domain.model.ErrorResponse;
import com.search.extension.apiSearch.domain.model.KakaoBlogSearchResultDTO;
import com.search.extension.apiSearch.domain.model.NaverBlogSearchResultDTO;

import reactor.core.publisher.Mono;

/**
 * 카카오/네이버 공급자 서비스의 요청 매핑과 응답 가공 테스트
 * WebClient 의 ExchangeFunction 을 로컬 스텁으로 바꿔 네트워크 없이 검증합니다.
 */
public class BlogSearchProviderHttpTest {
	private final List<URI> requests = new ArrayList<>();

	@Test
	void kakaoRequestUsesOneBasedPageAndCapsTotals() {
		KakaoBlogSearchServiceImpl kakao = new KakaoBlogSearchServiceImpl();
		ReflectionTestUtils.setField(kakao, "apiEndpoint", "/v2/search/blog");
		ReflectionTestUtils.setField(kakao, "kakaoApiWebClient", stubClient(fixture("kakao-blog-response.json")));

		Map<String, Object> result = kakao.getApiSearchResults("spring", "recency", PageRequest.of(2, 10));

		Map<String, String> query = queryOf(requests.get(0));
		assertEquals("/v2/search/blog", requests.get(0).getPath());
		assertEquals("spring", query.get("query"));
		assertEquals("recency", query.get("sort"));
		assertEquals("2", query.get("page"));
		assertEquals("10", query.get("size"));

		assertEquals(2, result.get("currentPage"));
		assertEquals(2500, result.get("totalItems"));  // 3200 → 2500 상한
		assertEquals(50, result.get("totalPages"));    // 320 → 50 상한
		// HashMap 순회 순서 = JSON 필드 순서 (portfolio-demo 모의 어댑터가 같은 순서를 사용)
		assertEquals(List.of("searchResult", "totalItems", "totalPages", "currentPage"), new ArrayList<>(result.keySet()));
		KakaoBlogSearchResultDTO body = (KakaoBlogSearchResultDTO) result.get("searchResult");
		assertEquals(3200, body.getMeta().getTotal_count());
		assertEquals("false", body.getMeta().getIs_end());
		assertEquals("모의 블로그", body.getDocuments().get(0).getBlogname());
	}

	@Test
	void kakaoPageBeyondResultsThrowsPageOutOfBounds() {
		KakaoBlogSearchServiceImpl kakao = new KakaoBlogSearchServiceImpl();
		ReflectionTestUtils.setField(kakao, "apiEndpoint", "/v2/search/blog");
		ReflectionTestUtils.setField(kakao, "kakaoApiWebClient",
				stubClient("{\"meta\":{\"total_count\":15,\"pageable_count\":15,\"is_end\":true},\"documents\":[]}"));

		ApiRequestsFailedException ex = assertThrows(ApiRequestsFailedException.class,
				() -> kakao.getApiSearchResults("spring", "accuracy", PageRequest.of(3, 10)));
		assertEquals(ErrorResponse.PAGE_OUT_OF_BOUNDS, ex.getErrorResponse());
	}

	@Test
	void naverRequestMapsSortAndPassesPageAsStart() {
		NaverBlogSearchServiceImpl naver = new NaverBlogSearchServiceImpl();
		ReflectionTestUtils.setField(naver, "apiEndpoint", "/v1/search/blog");
		ReflectionTestUtils.setField(naver, "naverApiWebClient", stubClient(fixture("naver-blog-response.json")));

		Map<String, Object> result = naver.getApiSearchResults("spring", "recency", PageRequest.of(3, 10));

		Map<String, String> query = queryOf(requests.get(0));
		assertEquals("/v1/search/blog", requests.get(0).getPath());
		assertEquals("date", query.get("sort"));
		// 기존 동작: 외부 page 값을 네이버 start(시작 문서 위치)에 그대로 전달
		assertEquals("3", query.get("start"));
		assertEquals("10", query.get("display"));

		assertEquals(3, result.get("currentPage"));
		assertEquals(42, result.get("totalItems"));
		assertEquals(5, result.get("totalPages"));
		NaverBlogSearchResultDTO body = (NaverBlogSearchResultDTO) result.get("searchResult");
		assertEquals("모의 블로거", body.getItems().get(0).getBloggername());
	}

	@Test
	void naverAccuracyMapsToSim() {
		NaverBlogSearchServiceImpl naver = new NaverBlogSearchServiceImpl();
		ReflectionTestUtils.setField(naver, "apiEndpoint", "/v1/search/blog");
		ReflectionTestUtils.setField(naver, "naverApiWebClient", stubClient(fixture("naver-blog-response.json")));

		naver.getApiSearchResults("spring", "accuracy", PageRequest.of(1, 10));

		assertEquals("sim", queryOf(requests.get(0)).get("sort"));
	}

	@Test
	void providerHttpErrorPropagatesForFallback() {
		KakaoBlogSearchServiceImpl kakao = new KakaoBlogSearchServiceImpl();
		ReflectionTestUtils.setField(kakao, "apiEndpoint", "/v2/search/blog");
		WebClient failing = WebClient.builder()
				.exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.SERVICE_UNAVAILABLE).build()))
				.build();
		ReflectionTestUtils.setField(kakao, "kakaoApiWebClient", failing);

		RuntimeException ex = assertThrows(RuntimeException.class,
				() -> kakao.getApiSearchResults("spring", "accuracy", PageRequest.of(1, 10)));
		// 메시지가 있어야 서비스에서 PAGE_OUT_OF_BOUNDS 가 아닌 API_CALL_FAIL 로 분류된다
		assertNotNull(ex.getMessage());
	}

	private WebClient stubClient(String json) {
		return WebClient.builder()
				.exchangeFunction(request -> {
					requests.add(request.url());
					return Mono.just(ClientResponse.create(HttpStatus.OK)
							.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
							.body(json)
							.build());
				})
				.build();
	}

	private static Map<String, String> queryOf(URI uri) {
		return UriComponentsBuilder.fromUri(uri).build().getQueryParams().toSingleValueMap();
	}

	private static String fixture(String name) {
		try (InputStream in = BlogSearchProviderHttpTest.class.getResourceAsStream("/provider/" + name)) {
			assertNotNull(in, "fixture 없음: " + name);
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (java.io.IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
