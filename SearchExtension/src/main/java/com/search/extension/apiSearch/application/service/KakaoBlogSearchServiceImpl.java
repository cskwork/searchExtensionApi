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
import com.search.extension.apiSearch.application.port.KakaoBlogSearchService;
import com.search.extension.apiSearch.domain.model.ErrorResponse;
import com.search.extension.apiSearch.domain.model.KakaoBlogSearchResultDTO;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Service
@Log4j2
public class KakaoBlogSearchServiceImpl implements KakaoBlogSearchService {
	
	@Value("${kakao.api.blog-search-path}")
	private String apiEndpoint;

	@Autowired
    @Qualifier("kakaoApiWebClient")
    private WebClient kakaoApiWebClient;

	@Override
	public Map<String, Object> getApiSearchResults(String query, String sort, Pageable pageable) {
		int currentPage =  pageable.getPageNumber() ;
		int pageSize = pageable.getPageSize();
		UriComponentsBuilder uriBuilder = UriComponentsBuilder
				.fromPath(apiEndpoint)
				.queryParam("query", query)
				.queryParam("sort", sort)
				.queryParam("page", currentPage)
				.queryParam("size", pageSize);

		String url = uriBuilder.toUriString();
		log.info("URL : " + url);
		
	    Mono<KakaoBlogSearchResultDTO> responseMono = kakaoApiWebClient.get()
	            .uri(url)
	            .retrieve()
	            .bodyToMono(KakaoBlogSearchResultDTO.class);
	    
	    KakaoBlogSearchResultDTO responseEntity = responseMono.block();
	  
	    log.info("KAKAO SEARCH SUCCESS");
	    
		Map<String, Object> response = new HashMap<>();
		int totalItemCount =  responseEntity.getMeta().getTotal_count();
		int pageCount = calculatePagesCount( pageSize, totalItemCount);
		
		if ( currentPage > pageCount ) {
			throw new ApiRequestsFailedException(ErrorResponse.PAGE_OUT_OF_BOUNDS);
		}
		
		if(totalItemCount > 2500 ){
			totalItemCount = 2500;
		}
		if(pageCount > 50){
			pageCount = 50;
		}
		
	    response.put("searchResult", responseEntity);
	    response.put("currentPage", currentPage);
	    response.put("totalItems", totalItemCount);
	    response.put("totalPages", pageCount);
		return response;
	}
	
	public static int calculatePagesCount(long pageSize, long totalCount) {
		return totalCount < pageSize ? 1 : (int) Math.ceil((double) totalCount / (double) pageSize);
	}
}