package com.search.extension.apiSearch.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSearchKeywordHistory is a Querydsl query type for SearchKeywordHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSearchKeywordHistory extends EntityPathBase<SearchKeywordHistory> {

    private static final long serialVersionUID = -218769911L;

    public static final QSearchKeywordHistory searchKeywordHistory = new QSearchKeywordHistory("searchKeywordHistory");

    public final StringPath apiSource = createString("apiSource");

    public final NumberPath<Integer> count = createNumber("count", Integer.class);

    public final DateTimePath<java.sql.Timestamp> createdTime = createDateTime("createdTime", java.sql.Timestamp.class);

    public final StringPath keyword = createString("keyword");

    public final NumberPath<Long> keywordId = createNumber("keywordId", Long.class);

    public QSearchKeywordHistory(String variable) {
        super(SearchKeywordHistory.class, forVariable(variable));
    }

    public QSearchKeywordHistory(Path<? extends SearchKeywordHistory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSearchKeywordHistory(PathMetadata metadata) {
        super(SearchKeywordHistory.class, metadata);
    }

}

