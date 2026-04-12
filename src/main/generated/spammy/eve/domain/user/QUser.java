package spammy.eve.domain.user;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 1404997111L;

    public static final QUser user = new QUser("user");

    public final ListPath<spammy.eve.domain.character.Character, spammy.eve.domain.character.QCharacter> characters = this.<spammy.eve.domain.character.Character, spammy.eve.domain.character.QCharacter>createList("characters", spammy.eve.domain.character.Character.class, spammy.eve.domain.character.QCharacter.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.Instant> createdAt = createDateTime("createdAt", java.time.Instant.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

