package spammy.eve.domain.blueprint;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBlueprint is a Querydsl query type for Blueprint
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBlueprint extends EntityPathBase<Blueprint> {

    private static final long serialVersionUID = -2142159007L;

    public static final QBlueprint blueprint = new QBlueprint("blueprint");

    public final EnumPath<spammy.eve.domain.industry.ActivityType> activityType = createEnum("activityType", spammy.eve.domain.industry.ActivityType.class);

    public final NumberPath<Long> blueprintTypeId = createNumber("blueprintTypeId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<BlueprintItem, QBlueprintItem> items = this.<BlueprintItem, QBlueprintItem>createList("items", BlueprintItem.class, QBlueprintItem.class, PathInits.DIRECT2);

    public final NumberPath<Integer> maxProductionLimit = createNumber("maxProductionLimit", Integer.class);

    public final NumberPath<Integer> timeSeconds = createNumber("timeSeconds", Integer.class);

    public QBlueprint(String variable) {
        super(Blueprint.class, forVariable(variable));
    }

    public QBlueprint(Path<? extends Blueprint> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBlueprint(PathMetadata metadata) {
        super(Blueprint.class, metadata);
    }

}

