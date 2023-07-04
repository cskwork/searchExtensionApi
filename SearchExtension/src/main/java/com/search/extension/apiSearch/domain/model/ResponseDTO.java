package com.search.extension.apiSearch.domain.model;

import lombok.Data;

@Data
public class ResponseDTO<T> {
    private String message;
    private int status;
    private T data;
    
    public ResponseDTO(int status, String message) {
        this.message = message;
        this.status = status;
    }
    
    public ResponseDTO(String message, int status, T data) {
        this.message = message;
        this.status = status;
        this.data = data;
    }
}