package spammy.eve.global.aop;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QEsiMetadata is a Querydsl query type for EsiMetadata
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEsiMetadata extends EntityPathBase<EsiMetadata> {

    private static final long serialVersionUID = 1741763494L;

    public static final QEsiMetadata esiMetadata = new QEsiMetadata("esiMetadata");

    public final spammy.eve.global.domain.QBaseEntity _super = new spammy.eve.global.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath etag = createString("etag");

    public final DateTimePath<java.time.Instant> expires = createDateTime("expires", java.time.Instant.class);

    public final StringPath lastModified = createString("lastModified");

    public final StringPath path = createString("path");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QEsiMetadata(String variable) {
        super(EsiMetadata.class, forVariable(variable));
    }

    public QEsiMetadata(Path<? extends EsiMetadata> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEsiMetadata(PathMetadata metadata) {
        super(EsiMetadata.class, metadata);
    }

}

