package com.search.extension.apiSearch.application.service;

import com.search.extension.apiSearch.application.port.ApiSearchService;
import com.search.extension.apiSearch.domain.model.SearchResultDTO;
import com.search.extension.apiSearch.domain.model.SortingTypeDTO;

public class ApiSeachServiceImpl implements ApiSearchService {
    
	@Override
    public SearchResultDTO search(String keyword, SortingTypeDTO sortingType, int page, int pageSize) {
        // Implement Kakao API search logic
		SearchResultDTO searchResult = new SearchResultDTO();
		return searchResult;
    }
	
}
