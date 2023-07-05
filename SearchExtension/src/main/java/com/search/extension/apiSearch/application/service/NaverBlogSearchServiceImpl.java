package com.search.extension.apiSearch.application.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.search.extension.apiSearch.application.exception.ApiRequestsFailedException;
import com.search.extension.apiSearch.application.port.NaverBlogSearchService;
import com.search.extension.apiSearch.domain.model.ErrorResponse;
import com.search.extension.apiSearch.domain.model.NaverBlogSearchResultDTO;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Service
@Log4j2
public class NaverBlogSearchServiceImpl implements NaverBlogSearchService {

	@Value("${naver.api.blog-search-path}")
	private String apiEndpoint;

	@Autowired
	@Qualifier("naverApiWebClient")
	private WebClient naverApiWebClient;

	@Override
	public Map<String, Object> getApiSearchResults(String query, String sort, Pageable pageable) {
		int currentPage =  pageable.getPageNumber() ;
		int pageSize = pageable.getPageSize();
		
		if (sort.equals("accuracy")) {
			sort = "sim";
		} else if (sort.equals("recency")) {
			sort = "date";
		};

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath(apiEndpoint)
				.queryParam("query", query)
				.queryParam("sort", sort)
				.queryParam("start", pageable.getPageNumber())
				.queryParam("display", pageSize);

		String url = uriBuilder.toUriString();
		log.info("URL : " + url);

		Mono<NaverBlogSearchResultDTO> responseMono = naverApiWebClient.get().uri(url).retrieve()
				.bodyToMono(NaverBlogSearchResultDTO.class);

		NaverBlogSearchResultDTO responseEntity = responseMono.block();
		log.info("NAVER SEARCH SUCCESS");

		Map<String, Object> response = new HashMap<>();

		int totalCount = responseEntity.getTotal();
		int totalPages = calculatePagesCount(pageSize , totalCount);

		if ( currentPage > totalPages ) {
			throw new ApiRequestsFailedException(ErrorResponse.PAGE_OUT_OF_BOUNDS);
		}

		int totalItemCount = responseEntity.getTotal();
		
		if(totalItemCount > 2500 ){
			totalItemCount = 2500;
		}
		
		if(totalPages > 50){
			totalPages = 50;
		}
		
		response.put("searchResult", responseEntity);
		response.put("currentPage", currentPage);
		response.put("totalItems", totalItemCount);
		response.put("totalPages", totalPages);

		return response;
	}

	public static int calculatePagesCount(long pageSize, long totalCount) {
		return totalCount < pageSize ? 1 : (int) Math.ceil((double) totalCount / (double) pageSize);
	}
}
