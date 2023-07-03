package com.search.extension.apiSearch.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.search.extension.apiSearch.application.port.KakaoBlogSearchService;
import com.search.extension.apiSearch.domain.model.BlogSearchResultDTO;
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
	public BlogSearchResultDTO getApiSearchResults(String query, String sort, int page, int size) {
		UriComponentsBuilder uriBuilder = UriComponentsBuilder
				.fromPath(apiEndpoint)
				.queryParam("query", query)
				.queryParam("sort", sort)
				.queryParam("page", page)
				.queryParam("size", size);

		String url = uriBuilder.toUriString();
		log.info("URL : " + url);

	    Mono<KakaoBlogSearchResultDTO> responseMono = kakaoApiWebClient.get()
	            .uri(url)
	            .retrieve()
	            .bodyToMono(KakaoBlogSearchResultDTO.class);

	    KakaoBlogSearchResultDTO responseEntity = responseMono.block();
	    log.info("KAKAO SEARCH SUCCESS");
		return responseEntity;
	}
}