package spammy.eve.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QIndustryJob is a Querydsl query type for IndustryJob
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QIndustryJob extends EntityPathBase<IndustryJob> {

    private static final long serialVersionUID = 467845224L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QIndustryJob industryJob = new QIndustryJob("industryJob");

    public final NumberPath<Integer> activityId = createNumber("activityId", Integer.class);

    public final NumberPath<Long> blueprintLocationId = createNumber("blueprintLocationId", Long.class);

    public final NumberPath<Long> blueprintTypeId = createNumber("blueprintTypeId", Long.class);

    public final spammy.eve.domain.character.QCharacter character;

    public final NumberPath<Long> completedCharacterId = createNumber("completedCharacterId", Long.class);

    public final DateTimePath<java.time.Instant> completedDate = createDateTime("completedDate", java.time.Instant.class);

    public final NumberPath<Double> cost = createNumber("cost", Double.class);

    public final NumberPath<Integer> duration = createNumber("duration", Integer.class);

    public final DateTimePath<java.time.Instant> endDate = createDateTime("endDate", java.time.Instant.class);

    public final NumberPath<Long> facilityId = createNumber("facilityId", Long.class);

    public final NumberPath<Long> jobId = createNumber("jobId", Long.class);

    public final NumberPath<Integer> licensedRuns = createNumber("licensedRuns", Integer.class);

    public final NumberPath<Long> outputLocationId = createNumber("outputLocationId", Long.class);

    public final DateTimePath<java.time.Instant> pauseDate = createDateTime("pauseDate", java.time.Instant.class);

    public final NumberPath<Double> probability = createNumber("probability", Double.class);

    public final NumberPath<Long> productTypeId = createNumber("productTypeId", Long.class);

    public final NumberPath<Integer> runs = createNumber("runs", Integer.class);

    public final DateTimePath<java.time.Instant> startDate = createDateTime("startDate", java.time.Instant.class);

    public final NumberPath<Long> stationId = createNumber("stationId", Long.class);

    public final StringPath status = createString("status");

    public final NumberPath<Integer> successfulRuns = createNumber("successfulRuns", Integer.class);

    public QIndustryJob(String variable) {
        this(IndustryJob.class, forVariable(variable), INITS);
    }

    public QIndustryJob(Path<? extends IndustryJob> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QIndustryJob(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QIndustryJob(PathMetadata metadata, PathInits inits) {
        this(IndustryJob.class, metadata, inits);
    }

    public QIndustryJob(Class<? extends IndustryJob> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.character = inits.isInitialized("character") ? new spammy.eve.domain.character.QCharacter(forProperty("character"), inits.get("character")) : null;
    }

}

