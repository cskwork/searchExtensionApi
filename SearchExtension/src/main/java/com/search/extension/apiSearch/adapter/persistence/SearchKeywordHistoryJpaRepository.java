package com.search.extension.apiSearch.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.search.extension.apiSearch.domain.SearchKeywordHistory;

public interface SearchKeywordHistoryJpaRepository extends JpaRepository<SearchKeywordHistory, Long> {
	
}

