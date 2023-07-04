package com.search.extension.apiSearch.domain.model;

import lombok.Data;

@Data
public class PopularKeywordDTO {
    public PopularKeywordDTO(String keyword, int count) {
		super();
		this.keyword = keyword;
		this.count = count;
	}
	private String keyword;
    private int count;
    
}
