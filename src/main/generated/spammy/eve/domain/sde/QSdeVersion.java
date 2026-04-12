package spammy.eve.domain.sde;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSdeVersion is a Querydsl query type for SdeVersion
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSdeVersion extends EntityPathBase<SdeVersion> {

    private static final long serialVersionUID = 1891985397L;

    public static final QSdeVersion sdeVersion = new QSdeVersion("sdeVersion");

    public final StringPath etag = createString("etag");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath lastModified = createString("lastModified");

    public final DateTimePath<java.time.Instant> updatedAt = createDateTime("updatedAt", java.time.Instant.class);

    public QSdeVersion(String variable) {
        super(SdeVersion.class, forVariable(variable));
    }

    public QSdeVersion(Path<? extends SdeVersion> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSdeVersion(PathMetadata metadata) {
        super(SdeVersion.class, metadata);
    }

}

