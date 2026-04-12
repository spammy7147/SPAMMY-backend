package spammy.eve.domain.market;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMarketPrice is a Querydsl query type for MarketPrice
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMarketPrice extends EntityPathBase<MarketPrice> {

    private static final long serialVersionUID = -1721719822L;

    public static final QMarketPrice marketPrice = new QMarketPrice("marketPrice");

    public final NumberPath<Double> adjustedPrice = createNumber("adjustedPrice", Double.class);

    public final NumberPath<Double> averagePrice = createNumber("averagePrice", Double.class);

    public final NumberPath<Long> typeId = createNumber("typeId", Long.class);

    public final DateTimePath<java.time.Instant> updatedAt = createDateTime("updatedAt", java.time.Instant.class);

    public QMarketPrice(String variable) {
        super(MarketPrice.class, forVariable(variable));
    }

    public QMarketPrice(Path<? extends MarketPrice> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMarketPrice(PathMetadata metadata) {
        super(MarketPrice.class, metadata);
    }

}

