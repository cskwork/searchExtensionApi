package com.search.extension.apiSearch.application.port;

import com.search.extension.apiSearch.domain.model.SearchResultDTO;
import com.search.extension.apiSearch.domain.model.SortingTypeDTO;

public interface ApiSearchService {
	
	SearchResultDTO search(String keyword, SortingTypeDTO sortingType, int page, int pageSize);
	
}
