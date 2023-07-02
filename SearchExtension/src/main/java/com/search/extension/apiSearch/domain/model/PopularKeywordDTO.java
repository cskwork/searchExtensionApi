package com.search.extension.apiSearch.domain.model;

import lombok.Data;

@Data
public class PopularKeywordDTO {
    private String keyword;
    private int frequency;
}
