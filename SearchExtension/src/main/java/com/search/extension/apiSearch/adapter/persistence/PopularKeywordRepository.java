package com.search.extension.apiSearch.adapter.persistence;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.search.extension.apiSearch.domain.SearchKeywordHistory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PopularKeywordRepository {
	//private final PopularKeywordJpaRepository contentJpaRepository;
	private final PopularKeywordQueryRepository contentQueryRepository;


	public List<Object[]> groupByApiSourceForKeyword(String keyword) {
		//  contentJpaRepository.save();
		  
		  QSearchKeywordHistory searchKeywordHistory = QSearchKeywordHistory.searchKeywordHistory;
		    contentQueryRepository.select(searchKeywordHistory.apiSource, searchKeywordHistory.count.sum())
			.from(searchKeywordHistory).where(searchKeywordHistory.keyword.eq(keyword))
			.groupBy(searchKeywordHistory.apiSource).fetch();
		    
		

//		return queryFactory.select(searchKeywordHistory.apiSource, searchKeywordHistory.count.sum())
//				.from(searchKeywordHistory).where(searchKeywordHistory.keyword.eq(keyword))
//				.groupBy(searchKeywordHistory.apiSource).fetch();
		return null;
	}
}
