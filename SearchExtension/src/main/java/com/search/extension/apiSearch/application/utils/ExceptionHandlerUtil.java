package com.search.extension.apiSearch.application.utils;

import com.search.extension.apiSearch.application.exception.ApiRequestsFailedException;
import com.search.extension.apiSearch.domain.model.ErrorResponse;

public class ExceptionHandlerUtil {
    
	public static boolean isValidParameter(String sort, int size, int page) {
		if (!(sort.equals("accuracy") || sort.equals("recency"))) {
			throw new ApiRequestsFailedException(ErrorResponse.INVALID_PARAMETER_SORT);
		}
		if ( (size > 50 || size < 1)  || (page > 50 || page < 1) ){
			throw new ApiRequestsFailedException(ErrorResponse.INVALID_PARAMETER_PAGE);
		}
		return true;
	};
}
