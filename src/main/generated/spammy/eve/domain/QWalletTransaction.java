package spammy.eve.domain;

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

    private static final long serialVersionUID = 336642510L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QWalletTransaction walletTransaction = new QWalletTransaction("walletTransaction");

    public final spammy.eve.domain.character.QCharacter character;

    public final NumberPath<Long> clientId = createNumber("clientId", Long.class);

    public final DateTimePath<java.time.Instant> date = createDateTime("date", java.time.Instant.class);

    public final BooleanPath isBuy = createBoolean("isBuy");

    public final BooleanPath isPersonal = createBoolean("isPersonal");

    public final NumberPath<Long> journalRefId = createNumber("journalRefId", Long.class);

    public final NumberPath<Long> locationId = createNumber("locationId", Long.class);

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final NumberPath<Long> transactionId = createNumber("transactionId", Long.class);

    public final NumberPath<Long> typeId = createNumber("typeId", Long.class);

    public final NumberPath<Double> unitPrice = createNumber("unitPrice", Double.class);

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
        this.character = inits.isInitialized("character") ? new spammy.eve.domain.character.QCharacter(forProperty("character"), inits.get("character")) : null;
    }

}

