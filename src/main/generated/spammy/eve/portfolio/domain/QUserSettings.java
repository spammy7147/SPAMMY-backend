package spammy.eve.portfolio.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserSettings is a Querydsl query type for UserSettings
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserSettings extends EntityPathBase<UserSettings> {

    private static final long serialVersionUID = -453653281L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserSettings userSettings = new QUserSettings("userSettings");

    public final spammy.eve.global.domain.QBaseEntity _super = new spammy.eve.global.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath iskAbbreviation = createBoolean("iskAbbreviation");

    public final StringPath timezone = createString("timezone");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUser user;

    public QUserSettings(String variable) {
        this(UserSettings.class, forVariable(variable), INITS);
    }

    public QUserSettings(Path<? extends UserSettings> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserSettings(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserSettings(PathMetadata metadata, PathInits inits) {
        this(UserSettings.class, metadata, inits);
    }

    public QUserSettings(Class<? extends UserSettings> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user"), inits.get("user")) : null;
    }

}

