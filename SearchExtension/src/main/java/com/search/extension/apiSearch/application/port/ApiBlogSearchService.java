package com.search.extension.apiSearch.application.port;

import java.util.List;

import com.search.extension.apiSearch.domain.SearchKeywordHistory;
import com.search.extension.apiSearch.domain.model.BlogSearchResultDTO;
import com.search.extension.apiSearch.domain.model.PopularKeywordDTO;

public interface ApiBlogSearchService {
	
	// GET 		==================================
	BlogSearchResultDTO getApiSearchResults(String query, String sort, int page, int size);
	
	List<PopularKeywordDTO> getPopularKeyword();
	
	// POST 	==================================
	SearchKeywordHistory addPopularKeyword(String keyword, Integer count, String apiSource);
}
