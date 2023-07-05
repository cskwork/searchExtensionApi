package com.search.extension.apiSearch.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum  ErrorResponse {
    //400-499 클라이언트 요청 이슈
    INVALID_NULL_PARAMETER(400, "필수 파라미터가 없습니다. 확인해주세요."),
    INVALID_PARAMETER_SORT(401, "순서 정렬 파라미터 값을 확인해주세요. (accuracy, recency)"),
    INVALID_PARAMETER_PAGE(402, "페이징 또는 페이지 범위 파라미터 값을 확인해주세요. (1-50)"),
    PAGE_OUT_OF_BOUNDS(402, "전체 검색 대상 페이지 값을 초과했습니다."),

    //500-599 서버 에러
    INTERNAL_SERVER_ERROR(500, "서버 에러입니다. 서버 팀 000에게 연락주세요!"),
    API_CALL_FAIL(501, "서버 에러입니다. API 요청에 실패했습니다");
    
    private final int status;
    private final String message;
}
