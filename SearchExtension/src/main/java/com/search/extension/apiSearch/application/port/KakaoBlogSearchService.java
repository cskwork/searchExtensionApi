package com.search.extension.apiSearch.application.port;

import com.search.extension.apiSearch.domain.model.BlogSearchResultDTO;

public interface KakaoBlogSearchService {
	
	BlogSearchResultDTO getApiSearchResults(String query, String sort, int page, int size);
	
}
