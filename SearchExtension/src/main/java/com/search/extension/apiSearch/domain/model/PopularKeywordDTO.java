package com.search.extension.apiSearch.domain.model;

import lombok.Data;

@Data
public class PopularKeywordDTO {
    private String keyword;
    private int count;
    
    public PopularKeywordDTO(String keyword, int count) {
        this.keyword = keyword;
        this.count = count;
    }
}

