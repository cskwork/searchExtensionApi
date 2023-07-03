package com.search.extension.apiSearch.application.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.search.extension.apiSearch.application.port.ApiBlogSearchService;
import com.search.extension.apiSearch.application.port.NaverBlogSearchService;
import com.search.extension.apiSearch.domain.model.BlogSearchResultDTO;
import com.search.extension.apiSearch.domain.model.NaverBlogSearchResultDTO;

import reactor.core.publisher.Mono;

@Service
public class NaverBlogSearchServiceImpl implements NaverBlogSearchService {
    private static final Logger log = LogManager.getLogger(NaverBlogSearchServiceImpl.class);

    private ApiBlogSearchService apiBlogSearchService;

	@Value("${naver.api.blog-search-path}")
	private String apiEndpoint;
	
	@Autowired
    @Qualifier("naverApiWebClient")
    private WebClient naverApiWebClient;
	
	@Override
	public BlogSearchResultDTO search(String query, String sort, int start, int display) {
		
		if(sort.equals("accuracy")) {
			sort = "sim";
		}else if(sort.equals("recency")) {
			sort = "date";
		};
		
		UriComponentsBuilder uriBuilder = UriComponentsBuilder
				.fromPath(apiEndpoint)
				.queryParam("query", query)
				.queryParam("sort", sort)
				.queryParam("start", start)
				.queryParam("display", display);

		String url = uriBuilder.toUriString();
		log.info("URL : " + url);

	    Mono<NaverBlogSearchResultDTO> responseMono = naverApiWebClient.get()
	            .uri(url)
	            .retrieve()
	            .bodyToMono(NaverBlogSearchResultDTO.class);

	    NaverBlogSearchResultDTO responseEntity = responseMono.block();
	    log.info("NAVER SEARCH SUCCESS");
		return responseEntity;
	}
}
