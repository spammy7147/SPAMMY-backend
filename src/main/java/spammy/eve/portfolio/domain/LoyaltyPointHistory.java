package spammy.eve.portfolio.domain;

import jakarta.persistence.*;
import lombok.*;
import spammy.eve.global.domain.BaseEntity;

@Entity
@Table(
    name = "loyalty_point_history",
    indexes = {
        @Index(name = "idx_lph_character_id", columnList = "character_id"),
        @Index(name = "idx_lph_char_corp", columnList = "character_id, corporation_id")
    }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoyaltyPointHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Character character;

    @Column(name = "corporation_id", nullable = false)
    private Long corporationId;

    @Column(name = "loyalty_points", nullable = false)
    private Integer loyaltyPoints;
}
