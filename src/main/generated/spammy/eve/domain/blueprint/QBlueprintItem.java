package spammy.eve.domain.blueprint;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBlueprintItem is a Querydsl query type for BlueprintItem
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBlueprintItem extends EntityPathBase<BlueprintItem> {

    private static final long serialVersionUID = 1830000148L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBlueprintItem blueprintItem = new QBlueprintItem("blueprintItem");

    public final QBlueprint blueprint;

    public final BooleanPath consumed = createBoolean("consumed");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<BlueprintItemKind> kind = createEnum("kind", BlueprintItemKind.class);

    public final NumberPath<Double> probability = createNumber("probability", Double.class);

    public final NumberPath<Long> qty = createNumber("qty", Long.class);

    public final spammy.eve.domain.sde.QType type;

    public QBlueprintItem(String variable) {
        this(BlueprintItem.class, forVariable(variable), INITS);
    }

    public QBlueprintItem(Path<? extends BlueprintItem> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBlueprintItem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBlueprintItem(PathMetadata metadata, PathInits inits) {
        this(BlueprintItem.class, metadata, inits);
    }

    public QBlueprintItem(Class<? extends BlueprintItem> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.blueprint = inits.isInitialized("blueprint") ? new QBlueprint(forProperty("blueprint")) : null;
        this.type = inits.isInitialized("type") ? new spammy.eve.domain.sde.QType(forProperty("type"), inits.get("type")) : null;
    }

}

