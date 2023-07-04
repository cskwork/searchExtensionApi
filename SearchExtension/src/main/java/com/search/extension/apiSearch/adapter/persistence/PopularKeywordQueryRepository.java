package com.search.extension.apiSearch.adapter.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.search.extension.apiSearch.domain.QSearchKeywordHistory;
import com.search.extension.apiSearch.domain.model.PopularKeywordDTO;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class PopularKeywordQueryRepository{
	private final JPAQueryFactory queryFactory;
	
	public List<PopularKeywordDTO> getGroupByApiSourceForKeyword() {
		QSearchKeywordHistory searchKeywordHistory = QSearchKeywordHistory.searchKeywordHistory;
		
		return queryFactory
	        .select(Projections.constructor(
	        		PopularKeywordDTO.class, 
	                searchKeywordHistory.keyword, 
	                searchKeywordHistory.count.sum())) 
	        .from(searchKeywordHistory)
	        .groupBy(searchKeywordHistory.keyword)
	        .orderBy(searchKeywordHistory.count.sum().desc())
	        .limit(10)
	        .fetch();
	}
}