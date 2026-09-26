package com.search.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SearchExtensionApplicationTests {

	@Value("${spring.datasource.url}")
	private String datasourceUrl;
	@Value("${kakao.api.base-url}")
	private String kakaoBaseUrl;
	@Value("${naver.api.base-url}")
	private String naverBaseUrl;
	@Value("${kakao.api.key}")
	private String kakaoKey;
	@Value("${naver.api.client-secret}")
	private String naverSecret;

	/**
	 * 컨텍스트가 테스트 전용 설정(src/test/resources/application.properties)으로만 뜨는지 확인
	 * 실패 메시지에 키 값을 출력하지 않습니다.
	 */
	@Test
	void contextLoadsWithIsolatedNoSecretConfig() {
		assertTrue(datasourceUrl.startsWith("jdbc:h2:mem:"), "테스트는 인메모리 H2 를 사용해야 합니다.");
		assertEquals("http://127.0.0.1:9", kakaoBaseUrl);
		assertEquals("http://127.0.0.1:9", naverBaseUrl);
		assertTrue(kakaoKey.startsWith("test-placeholder"), "테스트 설정이 실제 카카오 키를 읽고 있습니다.");
		assertTrue(naverSecret.startsWith("test-placeholder"), "테스트 설정이 실제 네이버 시크릿을 읽고 있습니다.");
	}
}
