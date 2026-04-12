package spammy.eve.domain.contract;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QContractItem is a Querydsl query type for ContractItem
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QContractItem extends EntityPathBase<ContractItem> {

    private static final long serialVersionUID = 775370506L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QContractItem contractItem = new QContractItem("contractItem");

    public final QCharacterContract contract;

    public final BooleanPath isIncluded = createBoolean("isIncluded");

    public final BooleanPath isSingleton = createBoolean("isSingleton");

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final NumberPath<Integer> rawQuantity = createNumber("rawQuantity", Integer.class);

    public final NumberPath<Long> recordId = createNumber("recordId", Long.class);

    public final NumberPath<Long> typeId = createNumber("typeId", Long.class);

    public QContractItem(String variable) {
        this(ContractItem.class, forVariable(variable), INITS);
    }

    public QContractItem(Path<? extends ContractItem> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QContractItem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QContractItem(PathMetadata metadata, PathInits inits) {
        this(ContractItem.class, metadata, inits);
    }

    public QContractItem(Class<? extends ContractItem> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.contract = inits.isInitialized("contract") ? new QCharacterContract(forProperty("contract"), inits.get("contract")) : null;
    }

}

