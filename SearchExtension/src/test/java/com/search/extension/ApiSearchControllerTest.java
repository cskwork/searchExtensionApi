package com.search.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import com.search.extension.apiSearch.adapter.persistence.PopularKeywordQueryRepository;
import com.search.extension.apiSearch.application.port.ApiBlogSearchService;
import com.search.extension.apiSearch.domain.model.PopularKeywordDTO;

@SpringBootTest
@AutoConfigureMockMvc
public class ApiSearchControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@MockBean
	private ApiBlogSearchService apiSearchService;
	@Autowired
	private PopularKeywordQueryRepository popularKeywordQueryRepository;
	
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

	@Test
	void testApiSearch() throws Exception {
		String query = "cat";
		String sort = "accuracy";
		int page = 1;
		int pageSize = 1;

        // HTTP GET 요청 Mock
		MockHttpServletResponse response = mockMvc
				.perform(get("/search")
				.param("query", query)
				.param("sort", sort)
				.param("page", String.valueOf(page))
				.param("pageSize", String.valueOf(pageSize)))
				.andReturn().getResponse();
		
		// 응답 확인 200
		assertEquals(HttpStatus.OK.value(), response.getStatus());
	}
	
    @Test
    void testGetPopularKeyword() throws Exception {
        // Stub 
        List<PopularKeywordDTO> expectedKeywords = Collections.singletonList(new PopularKeywordDTO("keyword", 10));
        when(apiSearchService.getPopularKeyword())
        	.thenReturn(expectedKeywords);

        // HTTP GET 요청 Mock
		MockHttpServletResponse response = mockMvc
				.perform(get("/popularKeyword"))
				.andReturn().getResponse();

		// 응답 확인 200
		assertEquals(HttpStatus.OK.value(), response.getStatus());
    }
	
}
