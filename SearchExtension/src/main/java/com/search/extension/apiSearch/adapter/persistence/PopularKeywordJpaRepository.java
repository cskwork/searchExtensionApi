package com.search.extension.apiSearch.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.search.extension.apiSearch.domain.PopularKeyword;

public interface PopularKeywordJpaRepository extends JpaRepository<PopularKeyword, Long> {
	
}

