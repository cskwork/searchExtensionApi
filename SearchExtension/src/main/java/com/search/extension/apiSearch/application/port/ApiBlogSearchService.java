package com.search.extension.apiSearch.application.port;

import java.util.List;

import com.search.extension.apiSearch.domain.PopularKeyword;
import com.search.extension.apiSearch.domain.SearchKeywordHistory;
import com.search.extension.apiSearch.domain.model.BlogSearchResultDTO;

public interface ApiBlogSearchService {
	
	// GET 		==================================
	BlogSearchResultDTO getApiSearchResults(String query, String sort, int page, int size);
	
	List<PopularKeyword> getPopularKeyword();
	
	// POST 	==================================
	SearchKeywordHistory addPopularKeyword(String keyword, Integer count, String apiSource);
}
