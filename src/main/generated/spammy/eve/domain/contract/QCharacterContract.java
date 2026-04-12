package spammy.eve.domain.contract;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCharacterContract is a Querydsl query type for CharacterContract
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCharacterContract extends EntityPathBase<CharacterContract> {

    private static final long serialVersionUID = 1251998390L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCharacterContract characterContract = new QCharacterContract("characterContract");

    public final NumberPath<Long> acceptorId = createNumber("acceptorId", Long.class);

    public final NumberPath<Long> assigneeId = createNumber("assigneeId", Long.class);

    public final spammy.eve.domain.character.QCharacter character;

    public final NumberPath<Double> collateral = createNumber("collateral", Double.class);

    public final NumberPath<Long> contractId = createNumber("contractId", Long.class);

    public final StringPath contractType = createString("contractType");

    public final DateTimePath<java.time.Instant> dateCompleted = createDateTime("dateCompleted", java.time.Instant.class);

    public final DateTimePath<java.time.Instant> dateExpired = createDateTime("dateExpired", java.time.Instant.class);

    public final DateTimePath<java.time.Instant> dateIssued = createDateTime("dateIssued", java.time.Instant.class);

    public final NumberPath<Long> endLocationId = createNumber("endLocationId", Long.class);

    public final BooleanPath forCorporation = createBoolean("forCorporation");

    public final NumberPath<Long> issuerId = createNumber("issuerId", Long.class);

    public final NumberPath<Double> price = createNumber("price", Double.class);

    public final NumberPath<Double> reward = createNumber("reward", Double.class);

    public final NumberPath<Long> startLocationId = createNumber("startLocationId", Long.class);

    public final StringPath status = createString("status");

    public final StringPath title = createString("title");

    public final NumberPath<Double> volume = createNumber("volume", Double.class);

    public QCharacterContract(String variable) {
        this(CharacterContract.class, forVariable(variable), INITS);
    }

    public QCharacterContract(Path<? extends CharacterContract> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCharacterContract(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCharacterContract(PathMetadata metadata, PathInits inits) {
        this(CharacterContract.class, metadata, inits);
    }

    public QCharacterContract(Class<? extends CharacterContract> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.character = inits.isInitialized("character") ? new spammy.eve.domain.character.QCharacter(forProperty("character"), inits.get("character")) : null;
    }

}

