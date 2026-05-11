package spammy.eve.character.domain;

import jakarta.persistence.*;
import lombok.*;
import spammy.eve.global.domain.BaseEntity;

import java.time.Instant;

@Entity
@Table(
    name = "standing",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_standing_character_from",
        columnNames = {"character_id", "from_id", "from_type"}
    ),
    indexes = {
        @Index(name = "idx_standing_character_id", columnList = "character_id")
    }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Standing extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Character character;

    @Column(name = "from_id", nullable = false)
    private Long fromId;

    @Column(name = "from_type", nullable = false, length = 50)
    private String fromType; // agent, npc_corp, faction

    @Column(name = "standing_value", nullable = false)
    private Double standingValue;

    public void update(Double standingValue) {
        this.standingValue = standingValue;
    }
}
