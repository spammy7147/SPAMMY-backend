package spammy.eve.character.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "asset",
    indexes = {
        @Index(name = "idx_asset_character_id", columnList = "character_id"),
        @Index(name = "idx_asset_type_id", columnList = "type_id"),
        @Index(name = "idx_asset_location_id", columnList = "location_id")
    }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Asset {

    @Id
    @Column(name = "item_id")
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Character character;

    @Column(name = "type_id", nullable = false)
    private Long typeId;

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Column(name = "location_type", length = 50)
    private String locationType; // station, solar_system, item, other

    @Column(name = "location_flag", length = 100)
    private String locationFlag;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "is_singleton", nullable = false)
    private Boolean isSingleton;

    @Column(name = "is_blueprint_copy")
    private Boolean isBlueprintCopy;
}