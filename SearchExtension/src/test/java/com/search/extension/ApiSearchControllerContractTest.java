package com.search.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.search.extension.apiSearch.adapter.web.ApiSearchController;
import com.search.extension.apiSearch.application.exception.GlobalExceptionHandler;
import com.search.extension.apiSearch.application.port.ApiBlogSearchService;
import com.search.extension.apiSearch.domain.model.ApiConstants;
import com.search.extension.apiSearch.domain.model.ErrorResponse;

/**
 * GET /search 파라미터 계약 테스트
 * 케이스 파일(contract/search-parameter-cases.json)은 portfolio-demo 모의 어댑터 테스트와 공유합니다.
 */
public class ApiSearchControllerContractTest {
	private static final String CASES = "/contract/search-parameter-cases.json";
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private MockMvc mockMvc;
	private ApiBlogSearchService apiSearchService;

	@BeforeEach
	void setUp() {
		apiSearchService = mock(ApiBlogSearchService.class);
		when(apiSearchService.getApiSearchResults(anyString(), anyString(), any()))
			.thenReturn(Map.of("currentPage", 1, "totalItems", 0, "totalPages", 1));
		ApiSearchController controller = new ApiSearchController();
		ReflectionTestUtils.setField(controller, "apiSearchService", apiSearchService);
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	static Stream<Arguments> cases() throws Exception {
		try (InputStream in = ApiSearchControllerContractTest.class.getResourceAsStream(CASES)) {
			assertNotNull(in, "계약 케이스 파일이 없습니다: " + CASES);
			JsonNode root = MAPPER.readTree(new String(in.readAllBytes(), StandardCharsets.UTF_8));
			List<Arguments> arguments = new ArrayList<>();
			root.get("cases").forEach(c -> arguments.add(Arguments.of(c.get("name").asText(), c)));
			return arguments.stream();
		}
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("cases")
	void searchParameterContract(String name, JsonNode c) throws Exception {
		MockHttpServletRequestBuilder request = get("/search");
		c.get("params").fields().forEachRemaining(p -> request.param(p.getKey(), p.getValue().asText()));

		MockHttpServletResponse response = mockMvc.perform(request).andReturn().getResponse();
		JsonNode body = MAPPER.readTree(response.getContentAsString(StandardCharsets.UTF_8));
		int expectedStatus = c.get("status").asInt();

		assertEquals(expectedStatus, response.getStatus());
		assertEquals(expectedStatus, body.get("status").asInt());

		if (expectedStatus == 200) {
			assertEquals(ApiConstants.SUCCESS, body.get("message").asText());
			// 외부 page 는 1부터 시작하는 값 그대로 Pageable.pageNumber 로 전달된다
			verify(apiSearchService).getApiSearchResults(
					eq(c.get("params").get("query").asText()),
					eq(c.get("sort").asText()),
					eq(PageRequest.of(c.get("page").asInt(), c.get("pageSize").asInt())));
		} else {
			ErrorResponse error = ErrorResponse.valueOf(c.get("error").asText());
			assertEquals(error.getStatus(), expectedStatus);
			assertEquals(error.getMessage(), body.get("message").asText());
			verify(apiSearchService, never()).getApiSearchResults(anyString(), anyString(), any());
		}
	}
}
