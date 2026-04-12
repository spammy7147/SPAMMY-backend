package spammy.eve.domain.blueprint;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCharacterBlueprint is a Querydsl query type for CharacterBlueprint
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCharacterBlueprint extends EntityPathBase<CharacterBlueprint> {

    private static final long serialVersionUID = 1192329308L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCharacterBlueprint characterBlueprint = new QCharacterBlueprint("characterBlueprint");

    public final spammy.eve.domain.character.QCharacter character;

    public final NumberPath<Long> itemId = createNumber("itemId", Long.class);

    public final StringPath locationFlag = createString("locationFlag");

    public final NumberPath<Long> locationId = createNumber("locationId", Long.class);

    public final NumberPath<Integer> materialEfficiency = createNumber("materialEfficiency", Integer.class);

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final NumberPath<Integer> runs = createNumber("runs", Integer.class);

    public final NumberPath<Integer> timeEfficiency = createNumber("timeEfficiency", Integer.class);

    public final NumberPath<Long> typeId = createNumber("typeId", Long.class);

    public QCharacterBlueprint(String variable) {
        this(CharacterBlueprint.class, forVariable(variable), INITS);
    }

    public QCharacterBlueprint(Path<? extends CharacterBlueprint> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCharacterBlueprint(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCharacterBlueprint(PathMetadata metadata, PathInits inits) {
        this(CharacterBlueprint.class, metadata, inits);
    }

    public QCharacterBlueprint(Class<? extends CharacterBlueprint> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.character = inits.isInitialized("character") ? new spammy.eve.domain.character.QCharacter(forProperty("character"), inits.get("character")) : null;
    }

}

