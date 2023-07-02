package com.search.extension.apiSearch.application.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.search.extension.apiSearch.application.port.NaverBlogSearchService;
import com.search.extension.apiSearch.domain.model.BlogSearchResultDTO;
import com.search.extension.apiSearch.domain.model.NaverBlogSearchResultDTO;

@Service
public class NaverBlogSearchServiceImpl implements NaverBlogSearchService {
	private final RestTemplate restTemplate;

	@Value("${naver.api.blog-search-path}")
	private String blogSearchPath;

	@Autowired
	public NaverBlogSearchServiceImpl(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	@Override
	public BlogSearchResultDTO search(String query, String sort, int start, int display) {
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath(blogSearchPath).queryParam("query", query)
				.queryParam("sort", sort).queryParam("start", start).queryParam("display", display);

		ResponseEntity<NaverBlogSearchResultDTO> responseEntity = restTemplate.getForEntity(uriBuilder.toUriString(),
				NaverBlogSearchResultDTO.class);

		NaverBlogSearchResultDTO naverBlogSearchResponse = responseEntity.getBody();

		if (responseEntity.getStatusCode() == HttpStatus.OK && naverBlogSearchResponse != null) {
			// Convert the KakaoBlogSearchResponse to SearchResult as necessary.
			NaverBlogSearchResultDTO searchResult = new NaverBlogSearchResultDTO();

			// Map the fields of KakaoBlogSearchResponse to SearchResult.
			// ...

			return searchResult;
		} else {
			// Handle error.
			// throw new Exception("Failed to search blogs using Kakao API.");
		}
		return naverBlogSearchResponse;

	}

	private static HttpURLConnection connect(String apiUrl) {
		try {
			URL url = new URL(apiUrl);
			return (HttpURLConnection) url.openConnection();
		} catch (MalformedURLException e) {
			throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
		} catch (IOException e) {
			throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
		}
	}

	private static String readBody(InputStream body) {
		InputStreamReader streamReader = new InputStreamReader(body);

		try (BufferedReader lineReader = new BufferedReader(streamReader)) {
			StringBuilder responseBody = new StringBuilder();

			String line;
			while ((line = lineReader.readLine()) != null) {
				responseBody.append(line);
			}

			return responseBody.toString();
		} catch (IOException e) {
			throw new RuntimeException("API 응답을 읽는 데 실패했습니다.", e);
		}
	}

}
