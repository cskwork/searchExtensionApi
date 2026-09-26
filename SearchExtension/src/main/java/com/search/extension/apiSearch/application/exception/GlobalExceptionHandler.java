package com.search.extension.apiSearch.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;

import com.search.extension.apiSearch.domain.model.ErrorResponse;
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
    
    // page, pageSize 에 숫자가 아닌 값이 들어오면 기존 페이징 검증 에러(402)로 응답
    @ExceptionHandler({ MethodArgumentTypeMismatchException.class })
    protected ResponseEntity<ErrorResponseDTO> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        if ("page".equals(ex.getName()) || "pageSize".equals(ex.getName())) {
            return handleApiRequestFailed(new ApiRequestsFailedException(ErrorResponse.INVALID_PARAMETER_PAGE));
        }
        return handleServerException(ex);
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
}
