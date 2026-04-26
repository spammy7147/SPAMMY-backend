package spammy.eve.character.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
    name = "market_order",
    indexes = {
        @Index(name = "idx_mo_character_id", columnList = "character_id"),
        @Index(name = "idx_mo_type_id", columnList = "type_id"),
        @Index(name = "idx_mo_is_buy_order", columnList = "is_buy_order")
    }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MarketOrder {

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Character character;

    @Column(name = "type_id", nullable = false)
    private Long typeId;

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Column(name = "region_id")
    private Long regionId;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "volume_total", nullable = false)
    private Integer volumeTotal;

    @Column(name = "volume_remain", nullable = false)
    private Integer volumeRemain;

    @Column(name = "is_buy_order", nullable = false)
    private Boolean isBuyOrder;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    @Column(name = "min_volume")
    private Integer minVolume;

    @Column(name = "`range`", length = 50)
    private String range;

    @Column(name = "issued", nullable = false)
    private Instant issued;

    @Column(name = "escrow")
    private Double escrow;

    @Column(name = "is_corporation", nullable = false)
    private Boolean isCorporation;

    public void updateVolume(Integer volumeRemain) {
        this.volumeRemain = volumeRemain;
    }
}