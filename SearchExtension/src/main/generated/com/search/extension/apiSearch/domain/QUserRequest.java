package com.search.extension.apiSearch.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserRequest is a Querydsl query type for UserRequest
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserRequest extends EntityPathBase<UserRequest> {

    private static final long serialVersionUID = 758118318L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserRequest userRequest = new QUserRequest("userRequest");

    public final DateTimePath<java.sql.Timestamp> createdTime = createDateTime("createdTime", java.sql.Timestamp.class);

    public final QSearchKeywordHistory popularKeyword;

    public final NumberPath<Integer> requestCount = createNumber("requestCount", Integer.class);

    public final StringPath requestIp = createString("requestIp");

    public final DateTimePath<java.sql.Timestamp> updatedTime = createDateTime("updatedTime", java.sql.Timestamp.class);

    public final StringPath userId = createString("userId");

    public QUserRequest(String variable) {
        this(UserRequest.class, forVariable(variable), INITS);
    }

    public QUserRequest(Path<? extends UserRequest> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserRequest(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserRequest(PathMetadata metadata, PathInits inits) {
        this(UserRequest.class, metadata, inits);
    }

    public QUserRequest(Class<? extends UserRequest> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.popularKeyword = inits.isInitialized("popularKeyword") ? new QSearchKeywordHistory(forProperty("popularKeyword")) : null;
    }

}

