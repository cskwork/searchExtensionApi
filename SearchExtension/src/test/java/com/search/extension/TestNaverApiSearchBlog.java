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
import com.search.extension.apiSearch.domain.model.NaverBlogSearchResultDTO;

/**
 * 실제 네이버 API 를 호출하는 opt-in 통합 테스트 (기본 test 태스크에서 제외)
 * 실행: SEARCH_API_LIVE_TESTS=true NAVER_CLIENT_ID=... NAVER_CLIENT_SECRET=... ./gradlew integrationTest
 */
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "SEARCH_API_LIVE_TESTS", matches = "true")
public class TestNaverApiSearchBlog {

	@Test
	void searchBlogMapsToDto() throws Exception {
		String clientId = System.getenv("NAVER_CLIENT_ID"); // 애플리케이션 클라이언트 아이디 (환경 변수)
		String clientSecret = System.getenv("NAVER_CLIENT_SECRET"); // 애플리케이션 클라이언트 시크릿 (환경 변수)
		assertTrue(clientId != null && !clientId.isBlank(), "NAVER_CLIENT_ID 환경 변수가 필요합니다.");
		assertTrue(clientSecret != null && !clientSecret.isBlank(), "NAVER_CLIENT_SECRET 환경 변수가 필요합니다.");

		String text = null;
		try {
			text = URLEncoder.encode("그린팩토리", "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("검색어 인코딩 실패", e);
		}

		String apiURL = "https://openapi.naver.com/v1/search/blog?query=" + text; // JSON 결과
		// String apiURL = "https://openapi.naver.com/v1/search/blog.xml?query="+ text;
		// // XML 결과

		Map<String, String> requestHeaders = new HashMap<>();
		requestHeaders.put("X-Naver-Client-Id", clientId);
		requestHeaders.put("X-Naver-Client-Secret", clientSecret);
		String responseBody = get(apiURL, requestHeaders);

		System.out.println(responseBody);

		ObjectMapper objectMapper = new ObjectMapper();
		NaverBlogSearchResultDTO dataDTO = objectMapper.readValue(responseBody, NaverBlogSearchResultDTO.class);
		assertNotNull(dataDTO.getItems(), "items 매핑 실패");
		assertTrue(dataDTO.getDisplay() >= 0, "display 매핑 실패");

		// Access the mapped data
		System.out.println("Last Build Date: " + dataDTO.getLastBuildDate());
		System.out.println("Total: " + dataDTO.getTotal());
		System.out.println("Start: " + dataDTO.getStart());
		System.out.println("Display: " + dataDTO.getDisplay());

		for (NaverBlogSearchResultDTO.Item item : dataDTO.getItems()) {
			System.out.println("Title: " + item.getTitle());
			System.out.println("Link: " + item.getLink());
			System.out.println("Description: " + item.getDescription());
			System.out.println("Blogger Name: " + item.getBloggername());
			System.out.println("Blogger Link: " + item.getBloggerlink());
			System.out.println("Post Date: " + item.getPostdate());
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
