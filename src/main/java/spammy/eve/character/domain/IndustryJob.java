package spammy.eve.character.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
    name = "industry_job",
    indexes = {
        @Index(name = "idx_ij_character_id", columnList = "character_id"),
        @Index(name = "idx_ij_status", columnList = "status"),
        @Index(name = "idx_ij_blueprint_type_id", columnList = "blueprint_type_id")
    }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IndustryJob {

    @Id
    @Column(name = "job_id")
    private Long jobId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Character character;

    @Column(name = "activity_id", nullable = false)
    private Integer activityId; // 1=제조, 3=TE연구, 4=ME연구, 5=복사, 8=인벤션

    @Column(name = "blueprint_type_id", nullable = false)
    private Long blueprintTypeId;

    @Column(name = "blueprint_location_id")
    private Long blueprintLocationId;

    @Column(name = "output_location_id")
    private Long outputLocationId;

    @Column(name = "facility_id")
    private Long facilityId;

    @Column(name = "station_id")
    private Long stationId;

    @Column(name = "runs", nullable = false)
    private Integer runs;

    @Column(name = "cost")
    private Double cost;

    @Column(name = "licensed_runs")
    private Integer licensedRuns;

    @Column(name = "probability")
    private Double probability;

    @Column(name = "product_type_id")
    private Long productTypeId;

    @Column(name = "status", nullable = false, length = 50)
    private String status; // active, cancelled, delivered, paused, ready, reverted

    @Column(name = "duration", nullable = false)
    private Integer duration;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Column(name = "pause_date")
    private Instant pauseDate;

    @Column(name = "completed_date")
    private Instant completedDate;

    @Column(name = "completed_character_id")
    private Long completedCharacterId;

    @Column(name = "successful_runs")
    private Integer successfulRuns;

    public void updateStatus(String status, Instant completedDate, Integer successfulRuns) {
        this.status = status;
        this.completedDate = completedDate;
        this.successfulRuns = successfulRuns;
    }
}