package com.search.extension.apiSearch.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.search.extension.apiSearch.application.port.KakaoBlogSearchService;
import com.search.extension.apiSearch.domain.model.BlogSearchResultDTO;
import com.search.extension.apiSearch.domain.model.KakaoBlogSearchResultDTO;

@Service
public class KakaoBlogSearchServiceImpl implements KakaoBlogSearchService {
	private final RestTemplate restTemplate;

	@Value("${kakao.api.blog-search-path}")
	private String blogSearchPath;

	@Autowired
	public KakaoBlogSearchServiceImpl(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Override
	public BlogSearchResultDTO search(String query, String sort, int page, int size) {
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath(blogSearchPath).queryParam("query", query)
				.queryParam("sort", sort).queryParam("page", page).queryParam("size", size);

		ResponseEntity<KakaoBlogSearchResultDTO> responseEntity = restTemplate.getForEntity(uriBuilder.toUriString(),
				KakaoBlogSearchResultDTO.class);

		KakaoBlogSearchResultDTO kakaoBlogSearchResponse = responseEntity.getBody();

		String responseBody =  responseEntity.getBody().toString();

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			KakaoBlogSearchResultDTO dataDTO = objectMapper.readValue(responseBody, KakaoBlogSearchResultDTO.class);

			// Access the mapped data
			System.out.println("Total Count: " + dataDTO.getMeta().getTotal_count());
			System.out.println("Pageable Count: " + dataDTO.getMeta().getPageable_count());
			System.out.println("Is End: " + dataDTO.getMeta().getIs_end());

			for (KakaoBlogSearchResultDTO.Document document : dataDTO.getDocuments()) {
				System.out.println("Title: " + document.getTitle());
				System.out.println("Contents: " + document.getContents());
				System.out.println("URL: " + document.getUrl());
				System.out.println("Blogname: " + document.getBlogname());
				System.out.println("Thumbnail: " + document.getThumbnail());
				System.out.println("Datetime: " + document.getDatetime());
				System.out.println("----------------------");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (responseEntity.getStatusCode() == HttpStatus.OK && kakaoBlogSearchResponse != null) {
			// Convert the KakaoBlogSearchResponse to SearchResult as necessary.
			KakaoBlogSearchResultDTO searchResult = new KakaoBlogSearchResultDTO();

			// Map the fields of KakaoBlogSearchResponse to SearchResult.
			// ...

			return searchResult;
		} else {
			// Handle error.
			// throw new Exception("Failed to search blogs using Kakao API.");
		}
		return kakaoBlogSearchResponse;

	}
}
