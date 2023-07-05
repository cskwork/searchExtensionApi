package com.search.extension.apiSearch.adapter.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.search.extension.apiSearch.domain.QPopularKeyword;
import com.search.extension.apiSearch.domain.model.PopularKeywordDTO;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class PopularKeywordQueryRepository{
	private final JPAQueryFactory queryFactory;
	
	/**
	 * 인기 키워드 리스트 조회 
	 * @return
	 */
	public List<PopularKeywordDTO> getGroupByApiSourceForKeyword() {
		QPopularKeyword popularKeywordList = QPopularKeyword.popularKeyword;
		
		return queryFactory
	        .select(Projections.constructor(
	        		PopularKeywordDTO.class, 
	                popularKeywordList.keyword 
	                ,popularKeywordList.count.sum()
	        		)) 
	        .from(popularKeywordList)
	        .groupBy(popularKeywordList.keyword)
	        .orderBy(popularKeywordList.count.sum().desc())
	        .limit(10)
	        .fetch();
	}
}