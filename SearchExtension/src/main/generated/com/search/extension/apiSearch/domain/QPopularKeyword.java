package com.search.extension.apiSearch.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPopularKeyword is a Querydsl query type for PopularKeyword
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPopularKeyword extends EntityPathBase<PopularKeyword> {

    private static final long serialVersionUID = -1446135514L;

    public static final QPopularKeyword popularKeyword = new QPopularKeyword("popularKeyword");

    public final StringPath apiSource = createString("apiSource");

    public final NumberPath<Integer> count = createNumber("count", Integer.class);

    public final DateTimePath<java.sql.Timestamp> createdTime = createDateTime("createdTime", java.sql.Timestamp.class);

    public final StringPath keyword = createString("keyword");

    public QPopularKeyword(String variable) {
        super(PopularKeyword.class, forVariable(variable));
    }

    public QPopularKeyword(Path<? extends PopularKeyword> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPopularKeyword(PathMetadata metadata) {
        super(PopularKeyword.class, metadata);
    }

}

