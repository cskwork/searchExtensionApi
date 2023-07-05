package com.search.extension.apiSearch.application.port;

import java.util.Map;

import org.springframework.data.domain.Pageable;

public interface KakaoBlogSearchService {
	
	Map<String, Object> getApiSearchResults(String query, String sort, Pageable pageable);
	
}
