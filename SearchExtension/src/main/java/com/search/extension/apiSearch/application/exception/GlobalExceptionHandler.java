package com.search.extension.apiSearch.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;

import com.search.extension.apiSearch.domain.model.ErrorResponseDTO;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
	
    @ExceptionHandler({ ApiRequestsFailedException.class })
    protected ResponseEntity<ErrorResponseDTO> handleApiRequestFailed(ApiRequestsFailedException ex) {
    	   return new ResponseEntity<>(new ErrorResponseDTO
    			   (
	    			   ex.getErrorResponse().getStatus()
	    			   , ex.getErrorResponse().getMessage()
    			   )
    			   , HttpStatus.valueOf(ex.getErrorResponse().getStatus()));
    }
    
    @ExceptionHandler({ Exception.class })
    protected ResponseEntity<ErrorResponseDTO> handleServerException(Exception ex) {
    	   return new ResponseEntity<>(new ErrorResponseDTO
    			   (
	    			   500
	    			   , ex.getMessage()
    			   )
    			   , HttpStatus.valueOf(500));
    }
    
    /*
     * MissingServletRequestParameterException.class: request parameter가 없을 때 에러를 리턴한다.
MissingRequestHeaderException.class: request header가 없을 때 에러를 리턴한다.
MethodArgumentNotValidException.class: request body의 데이터가 유효하지 않을 때 에러를 리턴한다.
NoHandlerFoundException.class: 404 error를 리턴한다.
     */
    
}