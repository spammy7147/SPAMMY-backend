package spammy.eve.portfolio.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QWalletJournal is a Querydsl query type for WalletJournal
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWalletJournal extends EntityPathBase<WalletJournal> {

    private static final long serialVersionUID = 842200909L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QWalletJournal walletJournal = new QWalletJournal("walletJournal");

    public final spammy.eve.global.domain.QBaseEntity _super = new spammy.eve.global.domain.QBaseEntity(this);

    public final NumberPath<Double> amount = createNumber("amount", Double.class);

    public final NumberPath<Double> balance = createNumber("balance", Double.class);

    public final QCharacter character;

    public final NumberPath<Long> contextId = createNumber("contextId", Long.class);

    public final StringPath contextIdType = createString("contextIdType");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.OffsetDateTime> date = createDateTime("date", java.time.OffsetDateTime.class);

    public final StringPath description = createString("description");

    public final NumberPath<Long> firstPartyId = createNumber("firstPartyId", Long.class);

    public final NumberPath<Long> journalId = createNumber("journalId", Long.class);

    public final StringPath refType = createString("refType");

    public final NumberPath<Long> secondPartyId = createNumber("secondPartyId", Long.class);

    public final NumberPath<Double> tax = createNumber("tax", Double.class);

    public final NumberPath<Long> taxReceiverId = createNumber("taxReceiverId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QWalletJournal(String variable) {
        this(WalletJournal.class, forVariable(variable), INITS);
    }

    public QWalletJournal(Path<? extends WalletJournal> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QWalletJournal(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QWalletJournal(PathMetadata metadata, PathInits inits) {
        this(WalletJournal.class, metadata, inits);
    }

    public QWalletJournal(Class<? extends WalletJournal> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.character = inits.isInitialized("character") ? new QCharacter(forProperty("character"), inits.get("character")) : null;
    }

}

