package com.search.extension.apiSearch.application.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.search.extension.apiSearch.application.port.NaverBlogSearchService;
import com.search.extension.apiSearch.domain.model.BlogSearchResultDTO;
import com.search.extension.apiSearch.domain.model.KakaoBlogSearchResultDTO;
import com.search.extension.apiSearch.domain.model.NaverBlogSearchResultDTO;

import reactor.core.publisher.Mono;

@Service
public class NaverBlogSearchServiceImpl implements NaverBlogSearchService {
    private static final Logger log = LogManager.getLogger(NaverBlogSearchServiceImpl.class);

	@Value("${naver.api.blog-search-path}")
	private String apiEndpoint;
	
	@Autowired
    @Qualifier("naverApiWebClient")
    private WebClient naverApiWebClient;
	
	@Override
	public BlogSearchResultDTO search(String query, String sort, int start, int display) {
		
		if(sort.equals("accurarcy")) {
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
		log.debug("URL : " + url);

	    Mono<NaverBlogSearchResultDTO> responseMono = naverApiWebClient.get()
	            .uri(url)
	            .retrieve()
	            .bodyToMono(NaverBlogSearchResultDTO.class);

	    NaverBlogSearchResultDTO responseEntity = responseMono.block();
		return responseEntity;
	}
}
