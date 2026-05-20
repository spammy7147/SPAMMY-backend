package spammy.eve.portfolio.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLoyaltyPointHistory is a Querydsl query type for LoyaltyPointHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLoyaltyPointHistory extends EntityPathBase<LoyaltyPointHistory> {

    private static final long serialVersionUID = -574018727L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLoyaltyPointHistory loyaltyPointHistory = new QLoyaltyPointHistory("loyaltyPointHistory");

    public final spammy.eve.global.domain.QBaseEntity _super = new spammy.eve.global.domain.QBaseEntity(this);

    public final QCharacter character;

    public final NumberPath<Long> corporationId = createNumber("corporationId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> loyaltyPoints = createNumber("loyaltyPoints", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QLoyaltyPointHistory(String variable) {
        this(LoyaltyPointHistory.class, forVariable(variable), INITS);
    }

    public QLoyaltyPointHistory(Path<? extends LoyaltyPointHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLoyaltyPointHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLoyaltyPointHistory(PathMetadata metadata, PathInits inits) {
        this(LoyaltyPointHistory.class, metadata, inits);
    }

    public QLoyaltyPointHistory(Class<? extends LoyaltyPointHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.character = inits.isInitialized("character") ? new QCharacter(forProperty("character"), inits.get("character")) : null;
    }

}

