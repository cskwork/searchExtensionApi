package com.search.extension.apiSearch.application.exception;

import com.search.extension.apiSearch.domain.model.ErrorResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ApiRequestsFailedException extends RuntimeException  {
	
	private static final long serialVersionUID = 1L;
	private final ErrorResponse errorResponse;
}