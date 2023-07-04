package com.search.extension.apiSearch.adapter.web;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.search.extension.apiSearch.application.port.ApiBlogSearchService;
import com.search.extension.apiSearch.domain.SearchKeywordHistory;
import com.search.extension.apiSearch.domain.model.BlogSearchResultDTO;
import com.search.extension.apiSearch.domain.model.PopularKeywordDTO;

@RestController
public class ApiSearchController {
	
	  @Autowired
	  private  ApiBlogSearchService apiSearchService;
	  
	  /**
	   * http://localhost:8080/search?query=abc&sort=accuracy&page=1&pageSize=1
	   * @param query
	   * @param sort (accuracy = 정확도순 ,recency = 최신순)
	   * @param page
	   * @param pageSize
	   * @return
	   * 
	   */
	  @GetMapping("/search")
	  public ResponseEntity<BlogSearchResultDTO> getApiSearchResults
	  		(
	          @RequestParam (value = "query", required = true) 		String query,
	          @RequestParam (value = "sort", required = false) 		String sort,
	          @RequestParam (value = "page", required = false) 		int page,
	          @RequestParam (value = "pageSize", required = false)  int pageSize
	        ) 
	  {
		  
	    BlogSearchResultDTO result = apiSearchService.getApiSearchResults(query, sort, page, pageSize);
	    return ResponseEntity.ok(result);
	  }
	  
	  /**
	   * http://localhost:8080/popularKeyword
	   * @return
	   */
	  @GetMapping("/popularKeyword")
	  public ResponseEntity<List<SearchKeywordHistory>> getPopularKeyword() 
	  {	  
		  List<SearchKeywordHistory> result = apiSearchService.getPopularKeyword();
		  return ResponseEntity.ok(result);
	  }
}

