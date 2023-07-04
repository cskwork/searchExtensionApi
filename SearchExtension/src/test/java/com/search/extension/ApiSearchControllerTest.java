package com.search.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.search.extension.apiSearch.adapter.web.ApiSearchController;
import com.search.extension.apiSearch.application.port.ApiBlogSearchService;
import com.search.extension.apiSearch.domain.model.BlogSearchResultDTO;
import com.search.extension.apiSearch.domain.model.KakaoBlogSearchResultDTO;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ApiSearchController.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ApiSearchControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@MockBean
	private ApiBlogSearchService apiSearchService;

	@Test
	void searchKakaoTest() throws Exception {
		String query = "cat";
		String sort = "accuracy";
		int page = 1;
		int pageSize = 1;
		
		BlogSearchResultDTO expectedResult = new KakaoBlogSearchResultDTO();

		when(apiSearchService.getApiSearchResults(query, sort, page, pageSize))
			.thenReturn(expectedResult);
		
        // HTTP GET 요청
		MockHttpServletResponse response = mockMvc
				.perform(get("/search")
				.param("query", query)
				.param("sort", sort)
				.param("page", String.valueOf(page))
				.param("pageSize", String.valueOf(pageSize)))
				.andReturn().getResponse();
		
	    // 요청 정상 확인 200
		assertEquals(HttpStatus.OK.value(), response.getStatus());
	}
}
