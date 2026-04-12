package spammy.eve.domain.blueprint;

import jakarta.persistence.*;
import lombok.*;
import spammy.eve.domain.character.Character;

@Entity
@Table(
    name = "character_blueprint",
    indexes = {
        @Index(name = "idx_cb_character_id", columnList = "character_id"),
        @Index(name = "idx_cb_type_id", columnList = "type_id")
    }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CharacterBlueprint {

    @Id
    @Column(name = "item_id")
    private Long itemId; // ESI item_id (asset item_id와 동일)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Character character;

    @Column(name = "type_id", nullable = false)
    private Long typeId;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "location_flag", length = 100)
    private String locationFlag;

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // BPO=-1, BPC=수량

    @Column(name = "time_efficiency", nullable = false)
    private Integer timeEfficiency; // TE 0~20

    @Column(name = "material_efficiency", nullable = false)
    private Integer materialEfficiency; // ME 0~10

    @Column(name = "runs")
    private Integer runs; // BPC 잔여 런, BPO=-1

    public boolean isBpo() {
        return runs == -1;
    }

    public void update(Integer materialEfficiency, Integer timeEfficiency,
                       Integer runs, Long locationId) {
        this.materialEfficiency = materialEfficiency;
        this.timeEfficiency = timeEfficiency;
        this.runs = runs;
        this.locationId = locationId;
    }
}