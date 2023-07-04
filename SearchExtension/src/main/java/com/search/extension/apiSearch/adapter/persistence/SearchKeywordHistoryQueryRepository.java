package com.search.extension.apiSearch.adapter.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.search.extension.apiSearch.domain.PopularKeyword;
import com.search.extension.apiSearch.domain.QSearchKeywordHistory;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class SearchKeywordHistoryQueryRepository{
	private final JPAQueryFactory queryFactory;
	
	/**
	 * 검색 히스토리 그룹 조회 
	 * @return
	 */
	public List<PopularKeyword> getGroupByApiSourceForKeyword() {
		QSearchKeywordHistory searchKeywordHistory = QSearchKeywordHistory.searchKeywordHistory;
        
		return queryFactory
	        .select(Projections.constructor(
	        		PopularKeyword.class, 
	                searchKeywordHistory.keyword, 
	                searchKeywordHistory.count.sum())) 
	        .from(searchKeywordHistory)
	        .groupBy(searchKeywordHistory.keyword)
	        .orderBy(searchKeywordHistory.count.sum().desc())
	        .limit(10)
	        .fetch();
	}
}