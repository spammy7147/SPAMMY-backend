package spammy.eve.domain.character;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCharacter is a Querydsl query type for Character
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCharacter extends EntityPathBase<Character> {

    private static final long serialVersionUID = -1683088115L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCharacter character = new QCharacter("character");

    public final StringPath accessToken = createString("accessToken");

    public final NumberPath<Long> allianceId = createNumber("allianceId", Long.class);

    public final StringPath allianceName = createString("allianceName");

    public final NumberPath<Double> balance = createNumber("balance", Double.class);

    public final NumberPath<Long> characterId = createNumber("characterId", Long.class);

    public final StringPath characterName = createString("characterName");

    public final NumberPath<Long> corporationId = createNumber("corporationId", Long.class);

    public final StringPath corporationName = createString("corporationName");

    public final DateTimePath<java.time.Instant> lastSyncedAt = createDateTime("lastSyncedAt", java.time.Instant.class);

    public final BooleanPath main = createBoolean("main");

    public final DateTimePath<java.time.Instant> omegaExpiresAt = createDateTime("omegaExpiresAt", java.time.Instant.class);

    public final StringPath ownerHash = createString("ownerHash");

    public final StringPath portraitUrl = createString("portraitUrl");

    public final StringPath refreshToken = createString("refreshToken");

    public final StringPath scopes = createString("scopes");

    public final DateTimePath<java.time.Instant> tokenExpiresAt = createDateTime("tokenExpiresAt", java.time.Instant.class);

    public final spammy.eve.domain.user.QUser user;

    public QCharacter(String variable) {
        this(Character.class, forVariable(variable), INITS);
    }

    public QCharacter(Path<? extends Character> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCharacter(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCharacter(PathMetadata metadata, PathInits inits) {
        this(Character.class, metadata, inits);
    }

    public QCharacter(Class<? extends Character> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new spammy.eve.domain.user.QUser(forProperty("user")) : null;
    }

}

