package com.search.extension.apiSearch.application.port;

import com.search.extension.apiSearch.domain.model.BlogSearchResultDTO;

public interface NaverBlogSearchService {
	
	BlogSearchResultDTO search(String query, String sort, int start, int display);
	
}
