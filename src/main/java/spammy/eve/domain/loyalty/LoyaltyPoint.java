package spammy.eve.domain.loyalty;

import jakarta.persistence.*;
import lombok.*;
import spammy.eve.domain.character.Character;

import java.time.Instant;

@Entity
@Table(
    name = "loyalty_point",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_lp_character_corporation",
        columnNames = {"character_id", "corporation_id"}
    ),
    indexes = {
        @Index(name = "idx_lp_character_id", columnList = "character_id")
    }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoyaltyPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Character character;

    @Column(name = "corporation_id", nullable = false)
    private Long corporationId; // LP를 준 NPC 코퍼레이션

    @Column(name = "loyalty_points", nullable = false)
    private Integer loyaltyPoints;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void update(Integer loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
        this.updatedAt = Instant.now();
    }
}