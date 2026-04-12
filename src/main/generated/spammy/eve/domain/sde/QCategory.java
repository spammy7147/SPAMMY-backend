package spammy.eve.domain.sde;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCategory is a Querydsl query type for Category
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCategory extends EntityPathBase<Category> {

    private static final long serialVersionUID = 1124620079L;

    public static final QCategory category = new QCategory("category");

    public final NumberPath<Long> iconId = createNumber("iconId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath nameEn = createString("nameEn");

    public final StringPath nameKo = createString("nameKo");

    public final BooleanPath published = createBoolean("published");

    public QCategory(String variable) {
        super(Category.class, forVariable(variable));
    }

    public QCategory(Path<? extends Category> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCategory(PathMetadata metadata) {
        super(Category.class, metadata);
    }

}

