package spammy.eve.domain;

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

    private static final long serialVersionUID = -142502361L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QWalletJournal walletJournal = new QWalletJournal("walletJournal");

    public final NumberPath<Double> amount = createNumber("amount", Double.class);

    public final NumberPath<Double> balance = createNumber("balance", Double.class);

    public final spammy.eve.domain.character.QCharacter character;

    public final NumberPath<Long> contextId = createNumber("contextId", Long.class);

    public final StringPath contextIdType = createString("contextIdType");

    public final DateTimePath<java.time.Instant> date = createDateTime("date", java.time.Instant.class);

    public final StringPath description = createString("description");

    public final NumberPath<Long> firstPartyId = createNumber("firstPartyId", Long.class);

    public final NumberPath<Long> journalId = createNumber("journalId", Long.class);

    public final StringPath refType = createString("refType");

    public final NumberPath<Long> secondPartyId = createNumber("secondPartyId", Long.class);

    public final NumberPath<Double> tax = createNumber("tax", Double.class);

    public final NumberPath<Long> taxReceiverId = createNumber("taxReceiverId", Long.class);

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
        this.character = inits.isInitialized("character") ? new spammy.eve.domain.character.QCharacter(forProperty("character"), inits.get("character")) : null;
    }

}

