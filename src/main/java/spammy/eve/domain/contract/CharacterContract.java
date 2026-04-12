package spammy.eve.domain.contract;

import jakarta.persistence.*;
import lombok.*;
import spammy.eve.domain.character.Character;

import java.time.Instant;

@Entity
@Table(
    name = "character_contract",
    indexes = {
        @Index(name = "idx_cc_character_id", columnList = "character_id"),
        @Index(name = "idx_cc_status", columnList = "status"),
        @Index(name = "idx_cc_type", columnList = "contract_type")
    }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CharacterContract {

    @Id
    @Column(name = "contract_id")
    private Long contractId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Character character;

    @Column(name = "contract_type", nullable = false, length = 50)
    private String contractType; // item_exchange, auction, courier, loan

    @Column(name = "status", nullable = false, length = 50)
    private String status; // outstanding, in_progress, finished 등

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "issuer_id")
    private Long issuerId;

    @Column(name = "assignee_id")
    private Long assigneeId;

    @Column(name = "acceptor_id")
    private Long acceptorId;

    @Column(name = "price")
    private Double price;

    @Column(name = "reward")
    private Double reward;

    @Column(name = "collateral")
    private Double collateral;

    @Column(name = "volume")
    private Double volume;

    @Column(name = "date_issued")
    private Instant dateIssued;

    @Column(name = "date_expired")
    private Instant dateExpired;

    @Column(name = "date_completed")
    private Instant dateCompleted;

    @Column(name = "start_location_id")
    private Long startLocationId;

    @Column(name = "end_location_id")
    private Long endLocationId;

    @Column(name = "for_corporation", nullable = false)
    private Boolean forCorporation;

    public void updateStatus(String status, Instant dateCompleted) {
        this.status = status;
        this.dateCompleted = dateCompleted;
    }
}