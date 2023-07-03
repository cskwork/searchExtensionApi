package com.search.extension.apiSearch.application.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

}
/*
public class ProblemDetail {
	private static final URI BLANK_TYPE = URI.create("about:blank");
	// 문제 유형을 식별하는 URI 참조, 정의하지 않으면 기본 값은 "about:blank"
	private URI type = BLANK_TYPE;
	// 문제 유형에 대한 사람이 읽을 수 있는 간단한 요약
	@Nullable
	private String title;
	// 이 문제의 응답 Http status 코드
	private int status;
	// 문제 유형에 대한 사람이 읽을 수 있는 간단한 설명
	@Nullable
	private String detail;
	// 문제가 발생한 URI
	@Nullable
	private URI instance;
	// 위에 선언한 문서 세부 정보 필드 이외에 추가적으로 사용할 확장 필드를 저장하는 곳
	@Nullable
	private Map<String, Object> properties;
}
*/
// https://www.sivalabs.in/spring-boot-3-error-reporting-using-problem-details/