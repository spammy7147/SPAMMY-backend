package spammy.eve.domain.blueprint;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import spammy.eve.domain.industry.ActivityType;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "blueprint",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_blueprint_id_activity", columnNames = {"blueprint_type_id", "activity_type"})
        },
        indexes = {
                @Index(name = "idx_blueprint_type_id", columnList = "blueprint_type_id"),
                @Index(name = "idx_blueprint_activity_type", columnList = "activity_type")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Blueprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blueprint_id", nullable = false)
    private Long id; // DB PK

    @Column(name = "blueprint_type_id", nullable = false)
    private Long blueprintTypeId; // 원본 _key

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 40)
    private ActivityType activityType;

    @Column(name = "time_seconds")
    private Integer timeSeconds;

    @Column(name = "max_production_limit")
    private Integer maxProductionLimit;

    @OneToMany(mappedBy = "blueprint", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlueprintItem> items = new ArrayList<>();

    public void addItem(BlueprintItem item) {
        items.add(item);
        item.setBlueprint(this);
    }

    public void removeItem(BlueprintItem item) {
        items.remove(item);
        item.setBlueprint(null);
    }
}