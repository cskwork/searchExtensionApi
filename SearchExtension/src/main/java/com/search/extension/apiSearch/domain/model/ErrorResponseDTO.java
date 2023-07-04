package com.search.extension.apiSearch.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ErrorResponseDTO {
    private final int status;
    private final String message;
}
