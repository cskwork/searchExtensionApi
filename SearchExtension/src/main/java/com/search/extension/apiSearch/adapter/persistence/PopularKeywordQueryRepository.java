package com.search.extension.apiSearch.adapter.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.search.extension.apiSearch.domain.QSearchKeywordHistory;
import com.search.extension.apiSearch.domain.SearchKeywordHistory;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Repository
//@RequiredArgsConstructor
@AllArgsConstructor
public class PopularKeywordQueryRepository{
	private final JPAQueryFactory queryFactory;
	
	public List<SearchKeywordHistory> getGroupByApiSourceForKeyword() {
		QSearchKeywordHistory searchKeywordHistory = QSearchKeywordHistory.searchKeywordHistory;
		return queryFactory
				.select(Projections.constructor(
						SearchKeywordHistory.class, 
						searchKeywordHistory.apiSource,
						searchKeywordHistory.count.sum()))
				.from(searchKeywordHistory)
				//.where(searchKeywordHistory.keyword.eq(keyword))
				.groupBy(searchKeywordHistory.apiSource)
				.fetch();
	}
}

//public class ContentQueryRepository {
//	  private final JPAQueryFactory queryFactory;
//	  
//	  public List<Content> findByTitle(String title) {
//	    return queryFactory
//	              .selectFrom(content)
//	              .where(content.title.eq(title))
//	              .fetch();
//	  }
//	}