package spammy.eve.domain.market;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMarketOrder is a Querydsl query type for MarketOrder
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMarketOrder extends EntityPathBase<MarketOrder> {

    private static final long serialVersionUID = -1722648073L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMarketOrder marketOrder = new QMarketOrder("marketOrder");

    public final spammy.eve.domain.character.QCharacter character;

    public final NumberPath<Integer> duration = createNumber("duration", Integer.class);

    public final NumberPath<Double> escrow = createNumber("escrow", Double.class);

    public final BooleanPath isBuyOrder = createBoolean("isBuyOrder");

    public final BooleanPath isCorporation = createBoolean("isCorporation");

    public final DateTimePath<java.time.Instant> issued = createDateTime("issued", java.time.Instant.class);

    public final NumberPath<Long> locationId = createNumber("locationId", Long.class);

    public final NumberPath<Integer> minVolume = createNumber("minVolume", Integer.class);

    public final NumberPath<Long> orderId = createNumber("orderId", Long.class);

    public final NumberPath<Double> price = createNumber("price", Double.class);

    public final StringPath range = createString("range");

    public final NumberPath<Long> regionId = createNumber("regionId", Long.class);

    public final NumberPath<Long> typeId = createNumber("typeId", Long.class);

    public final NumberPath<Integer> volumeRemain = createNumber("volumeRemain", Integer.class);

    public final NumberPath<Integer> volumeTotal = createNumber("volumeTotal", Integer.class);

    public QMarketOrder(String variable) {
        this(MarketOrder.class, forVariable(variable), INITS);
    }

    public QMarketOrder(Path<? extends MarketOrder> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMarketOrder(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMarketOrder(PathMetadata metadata, PathInits inits) {
        this(MarketOrder.class, metadata, inits);
    }

    public QMarketOrder(Class<? extends MarketOrder> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.character = inits.isInitialized("character") ? new spammy.eve.domain.character.QCharacter(forProperty("character"), inits.get("character")) : null;
    }

}

