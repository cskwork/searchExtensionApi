package com.search.extension.apiSearch.application.port;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.search.extension.apiSearch.domain.SearchKeywordHistory;
import com.search.extension.apiSearch.domain.model.PopularKeywordDTO;
import com.search.extension.apiSearch.domain.model.ResponseDTO;

public interface ApiBlogSearchService {
	
	// GET 		==================================
	Map<String, Object> getApiSearchResults(String query, String sort, Pageable pageable);
	
	List<PopularKeywordDTO> getPopularKeyword();
	
	// POST 	==================================
	SearchKeywordHistory addPopularKeyword(String keyword, Integer count, String apiSource);
}
