package spammy.eve.portfolio.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QWalletTransaction is a Querydsl query type for WalletTransaction
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWalletTransaction extends EntityPathBase<WalletTransaction> {

    private static final long serialVersionUID = -415162380L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QWalletTransaction walletTransaction = new QWalletTransaction("walletTransaction");

    public final spammy.eve.global.domain.QBaseEntity _super = new spammy.eve.global.domain.QBaseEntity(this);

    public final QCharacter character;

    public final NumberPath<Long> clientId = createNumber("clientId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.Instant> date = createDateTime("date", java.time.Instant.class);

    public final BooleanPath isBuy = createBoolean("isBuy");

    public final BooleanPath isPersonal = createBoolean("isPersonal");

    public final NumberPath<Long> journalRefId = createNumber("journalRefId", Long.class);

    public final NumberPath<Long> locationId = createNumber("locationId", Long.class);

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final NumberPath<Long> transactionId = createNumber("transactionId", Long.class);

    public final NumberPath<Long> typeId = createNumber("typeId", Long.class);

    public final NumberPath<Double> unitPrice = createNumber("unitPrice", Double.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QWalletTransaction(String variable) {
        this(WalletTransaction.class, forVariable(variable), INITS);
    }

    public QWalletTransaction(Path<? extends WalletTransaction> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QWalletTransaction(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QWalletTransaction(PathMetadata metadata, PathInits inits) {
        this(WalletTransaction.class, metadata, inits);
    }

    public QWalletTransaction(Class<? extends WalletTransaction> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.character = inits.isInitialized("character") ? new QCharacter(forProperty("character"), inits.get("character")) : null;
    }

}

