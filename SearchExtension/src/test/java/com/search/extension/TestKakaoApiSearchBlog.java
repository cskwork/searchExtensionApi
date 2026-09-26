package com.search.extension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.search.extension.apiSearch.domain.model.KakaoBlogSearchResultDTO;

/**
 * 실제 카카오 API 를 호출하는 opt-in 통합 테스트 (기본 test 태스크에서 제외)
 * 실행: SEARCH_API_LIVE_TESTS=true KAKAO_API_KEY=... ./gradlew integrationTest
 */
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "SEARCH_API_LIVE_TESTS", matches = "true")
public class TestKakaoApiSearchBlog {

	@Test
	void searchBlogMapsToDto() throws Exception {
		String clientSecret = System.getenv("KAKAO_API_KEY"); // 애플리케이션 REST API 키 (환경 변수)
		assertTrue(clientSecret != null && !clientSecret.isBlank(), "KAKAO_API_KEY 환경 변수가 필요합니다.");

		String text = null;
		try {
			text = URLEncoder.encode("그린팩토리", "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("검색어 인코딩 실패", e);
		}

		String apiURL = "https://dapi.kakao.com/v2/search/blog?query=" + text; // JSON 결과

		Map<String, String> requestHeaders = new HashMap<>();
		requestHeaders.put("Authorization", "KakaoAK " + clientSecret);
		String responseBody = get(apiURL, requestHeaders);

		System.out.println(responseBody);

		ObjectMapper objectMapper = new ObjectMapper();
		KakaoBlogSearchResultDTO dataDTO = objectMapper.readValue(responseBody, KakaoBlogSearchResultDTO.class);
		assertNotNull(dataDTO.getMeta(), "meta 매핑 실패");
		assertNotNull(dataDTO.getDocuments(), "documents 매핑 실패");

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

	}

	private static String get(String apiUrl, Map<String, String> requestHeaders) {
		HttpURLConnection con = connect(apiUrl);
		try {
			con.setRequestMethod("GET");
			for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
				con.setRequestProperty(header.getKey(), header.getValue());
			}

			int responseCode = con.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출
				return readBody(con.getInputStream());
			} else { // 오류 발생
				return readBody(con.getErrorStream());
			}
		} catch (IOException e) {
			throw new RuntimeException("API 요청과 응답 실패", e);
		} finally {
			con.disconnect();
		}
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
