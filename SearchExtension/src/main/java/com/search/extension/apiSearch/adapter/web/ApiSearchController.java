package com.search.extension.apiSearch.adapter.web;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.search.extension.apiSearch.application.exception.ApiRequestsFailedException;
import com.search.extension.apiSearch.application.port.ApiBlogSearchService;
import com.search.extension.apiSearch.domain.model.ApiConstants;
import com.search.extension.apiSearch.domain.model.BlogSearchResultDTO;
import com.search.extension.apiSearch.domain.model.ErrorResponse;
import com.search.extension.apiSearch.domain.model.PopularKeywordDTO;
import com.search.extension.apiSearch.domain.model.ResponseDTO;

import io.micrometer.common.util.StringUtils;

@RestController
public class ApiSearchController {
	  @Autowired
	  private  ApiBlogSearchService apiSearchService;
	  /**
	   * 키워드를 통해 블로그를 검색 (Sorting, Pagination 기능 지원)
	   * http://localhost:8080/search?query=abc&sort=accuracy&page=1&pageSize=1
	   * @param query
	   * @param sort (accuracy = 정확도순 ,recency = 최신순)
	   * @param page
	   * @param pageSize
	   * @return
	   */
	  @GetMapping("/search")
	  public ResponseEntity<ResponseDTO<BlogSearchResultDTO>> getApiSearchResults
	  		(
	          @RequestParam (value = "query", required = false) 					String query,
	          @RequestParam (value = "sort",     defaultValue = "accuracy", required = false) 	String sort,
	          @RequestParam (value = "page",     defaultValue = "1", required = false) 		int page,
	          @RequestParam (value = "pageSize", defaultValue = "10", required = false) 		int pageSize
	        ) 
	  {  
		if (StringUtils.isBlank(query)) {
			throw new ApiRequestsFailedException(ErrorResponse.INVALID_NULL_PARAMETER);
	    }
		BlogSearchResultDTO result = apiSearchService.getApiSearchResults(query, sort, page, pageSize);
		ResponseDTO<BlogSearchResultDTO> response = new ResponseDTO<>(ApiConstants.SUCCESS, 200, result);
		return ResponseEntity.ok(response);
	  }
	  
	  /**
	   * 사용자들이 많이 검색한 순서대로, 최대 10개의 검색 키워드를 제공 (인기 카운트도 조회)
	   * http://localhost:8080/popularKeyword
	   * @return
	   */
	  @GetMapping("/popularKeyword")
	  public  ResponseEntity<ResponseDTO<List<PopularKeywordDTO>>> getPopularKeyword() 
	  {	  
	    List<PopularKeywordDTO> result = apiSearchService.getPopularKeyword();
		ResponseDTO<List<PopularKeywordDTO>> response = new ResponseDTO<>(ApiConstants.SUCCESS, 200, result);
		return ResponseEntity.ok(response);    
	  }
}

