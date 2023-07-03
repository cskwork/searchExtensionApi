package com.search.extension.apiSearch.application.utils;

import org.springframework.http.HttpStatus;

public class GlobalExceptionHandlerUtil {
	
    public HttpStatus determineHttpStatus(int customCode) {
        if (customCode == 0) {
            return HttpStatus.valueOf(customCode);
        }
        return HttpStatus.BAD_REQUEST;
    }
}
