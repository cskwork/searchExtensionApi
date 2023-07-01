package com.search.extension.apiSearch.adapter.web;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.search.extension.apiSearch.application.port.ApiSearchService;
import com.search.extension.apiSearch.domain.model.SearchResultDTO;
import com.search.extension.apiSearch.domain.model.SortingTypeDTO;

@RestController
public class ApiSearchController {
	
	  @Autowired
	  private ApiSearchService apiSearchService;
	  
	
	  @GetMapping("search")
	  public ResponseEntity<SearchResultDTO> search(
	          @RequestParam String keyword,
	          @RequestParam SortingTypeDTO sortingType,
	          @RequestParam int page,
	          @RequestParam int pageSize) {
		  
	    SearchResultDTO result = apiSearchService.search(keyword, sortingType, page, pageSize);
	    return ResponseEntity.ok(result);
	  }
}
