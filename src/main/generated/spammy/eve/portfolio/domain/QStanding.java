package spammy.eve.portfolio.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStanding is a Querydsl query type for Standing
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStanding extends EntityPathBase<Standing> {

    private static final long serialVersionUID = 612796573L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStanding standing = new QStanding("standing");

    public final spammy.eve.global.domain.QBaseEntity _super = new spammy.eve.global.domain.QBaseEntity(this);

    public final QCharacter character;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> fromId = createNumber("fromId", Long.class);

    public final StringPath fromType = createString("fromType");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Double> standingValue = createNumber("standingValue", Double.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QStanding(String variable) {
        this(Standing.class, forVariable(variable), INITS);
    }

    public QStanding(Path<? extends Standing> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStanding(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStanding(PathMetadata metadata, PathInits inits) {
        this(Standing.class, metadata, inits);
    }

    public QStanding(Class<? extends Standing> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.character = inits.isInitialized("character") ? new QCharacter(forProperty("character"), inits.get("character")) : null;
    }

}

