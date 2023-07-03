package com.search.extension.apiSearch.application.port;

import com.search.extension.apiSearch.domain.PopularKeyword;
import com.search.extension.apiSearch.domain.model.BlogSearchResultDTO;

public interface ApiBlogSearchService {
	
	BlogSearchResultDTO search(String query, String sort, int page, int size);
	
	PopularKeyword addPopularKeyword(String keyword, Integer count, String apiSource);
	 
// MOVE TO SEPARATE PACKAGE
//	public List<PopularKeywordDTO> getPopularKeywords() {
//        return keywordSearchFrequencyRepository.findTop10ByOrderByFrequencyDesc();
//    }
}
