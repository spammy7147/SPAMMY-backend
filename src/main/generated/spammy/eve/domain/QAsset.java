package spammy.eve.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAsset is a Querydsl query type for Asset
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAsset extends EntityPathBase<Asset> {

    private static final long serialVersionUID = 1505394969L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAsset asset = new QAsset("asset");

    public final spammy.eve.domain.character.QCharacter character;

    public final BooleanPath isBlueprintCopy = createBoolean("isBlueprintCopy");

    public final BooleanPath isSingleton = createBoolean("isSingleton");

    public final NumberPath<Long> itemId = createNumber("itemId", Long.class);

    public final StringPath locationFlag = createString("locationFlag");

    public final NumberPath<Long> locationId = createNumber("locationId", Long.class);

    public final StringPath locationType = createString("locationType");

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final NumberPath<Long> typeId = createNumber("typeId", Long.class);

    public QAsset(String variable) {
        this(Asset.class, forVariable(variable), INITS);
    }

    public QAsset(Path<? extends Asset> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAsset(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAsset(PathMetadata metadata, PathInits inits) {
        this(Asset.class, metadata, inits);
    }

    public QAsset(Class<? extends Asset> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.character = inits.isInitialized("character") ? new spammy.eve.domain.character.QCharacter(forProperty("character"), inits.get("character")) : null;
    }

}

