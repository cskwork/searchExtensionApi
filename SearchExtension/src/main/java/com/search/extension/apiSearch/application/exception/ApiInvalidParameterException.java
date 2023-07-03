package com.search.extension.apiSearch.application.exception;

import java.net.URI;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import com.search.extension.apiSearch.application.utils.HttpUtil;

// https://www.sivalabs.in/spring-boot-3-error-reporting-using-problem-details/

/**
 * CUSTOM CODE 생성 가능 
 */
public class ApiInvalidParameterException extends ErrorResponseException {
	private static final long serialVersionUID = 1L;
	
	@Autowired 
	private  static HttpUtil httpUtil;

	public ApiInvalidParameterException(String failMessage) {
	    super(HttpStatus.BAD_REQUEST, asProblemDetail(HttpStatus.BAD_REQUEST, "Error with Search : " + failMessage), null);
	}
	
	public ApiInvalidParameterException(int customCode, String failMessage ) {
	    super(httpUtil.determineStatus(customCode), asProblemDetail(httpUtil.determineStatus(customCode), "Error with Search : " + failMessage), null);
	}
	
	private static ProblemDetail asProblemDetail(HttpStatus customStatus, String failMessage ) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, failMessage);
		problemDetail.setTitle("Api Invalid Parameter");
		// problemDetail.setType(URI.create("http://localhost:8080/errors/request-fail"));
		problemDetail.setProperty("errorCategory", "Generic");
		problemDetail.setProperty("timestamp", Instant.now());
		return problemDetail;
	}
}