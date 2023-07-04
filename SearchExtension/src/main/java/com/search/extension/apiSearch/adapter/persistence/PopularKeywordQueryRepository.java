package com.search.extension.apiSearch.adapter.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PopularKeywordQueryRepository {
	private final JPAQueryFactory jpaQueryFactory;
}