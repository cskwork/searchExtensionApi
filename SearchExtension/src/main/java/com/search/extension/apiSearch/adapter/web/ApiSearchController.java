package com.search.extension.apiSearch.adapter.web;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.search.extension.apiSearch.application.port.ApiBlogSearchService;
import com.search.extension.apiSearch.domain.model.BlogSearchResultDTO;

@RestController // http://localhost:8080/search?query=abc&sort=accuracy&page=1&pageSize=1
public class ApiSearchController {
	
	  @Autowired
	  private  ApiBlogSearchService apiSearchService;
	
	  // http://localhost:8080/search?query=abc&sort=accuracy&page=1&pageSize=1
	  /**
	   * 
	   * @param query
	   * @param sort (accuracy = 정확도순 ,recency = 최신순)
	   * @param page
	   * @param pageSize
	   * @return
	   */
	  @GetMapping("/search")
	  public ResponseEntity<BlogSearchResultDTO> search
	  		(
	          @RequestParam (value = "query", required = true) 		String query,
	          @RequestParam (value = "sort", required = false) 		String sort,
	          @RequestParam (value = "page", required = false) 		int page,
	          @RequestParam (value = "pageSize", required = false)  int pageSize
	        ) 
	  {
		  
	    BlogSearchResultDTO result = apiSearchService.search(query, sort, page, pageSize);
	    return ResponseEntity.ok(result);
	  }
}

