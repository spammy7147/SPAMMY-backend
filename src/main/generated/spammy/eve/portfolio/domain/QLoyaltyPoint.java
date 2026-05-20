package spammy.eve.portfolio.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLoyaltyPoint is a Querydsl query type for LoyaltyPoint
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLoyaltyPoint extends EntityPathBase<LoyaltyPoint> {

    private static final long serialVersionUID = -832563013L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLoyaltyPoint loyaltyPoint = new QLoyaltyPoint("loyaltyPoint");

    public final spammy.eve.global.domain.QBaseEntity _super = new spammy.eve.global.domain.QBaseEntity(this);

    public final QCharacter character;

    public final NumberPath<Long> corporationId = createNumber("corporationId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> loyaltyPoints = createNumber("loyaltyPoints", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QLoyaltyPoint(String variable) {
        this(LoyaltyPoint.class, forVariable(variable), INITS);
    }

    public QLoyaltyPoint(Path<? extends LoyaltyPoint> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLoyaltyPoint(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLoyaltyPoint(PathMetadata metadata, PathInits inits) {
        this(LoyaltyPoint.class, metadata, inits);
    }

    public QLoyaltyPoint(Class<? extends LoyaltyPoint> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.character = inits.isInitialized("character") ? new QCharacter(forProperty("character"), inits.get("character")) : null;
    }

}

