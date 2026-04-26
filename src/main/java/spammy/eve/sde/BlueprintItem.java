package spammy.eve.sde;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
  name = "blueprint_item",
  uniqueConstraints = @UniqueConstraint(
    name="uq_bp_item", columnNames={"blueprint_id","kind","type_id"}
  ),
  indexes = {
    @Index(name="idx_bpi_bp", columnList="blueprint_id"),
    @Index(name="idx_bpi_type", columnList="type_id"),
    @Index(name="idx_bpi_kind", columnList="kind")
  }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class BlueprintItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="blueprint_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
  private Blueprint blueprint;

  @Enumerated(EnumType.STRING)
  @Column(name="kind", nullable=false, length=20)
  private BlueprintItemKind kind;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="type_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
  private Type type;

  @Column(name="qty", nullable=false)
  private Long qty;

  @Column(name="is_consumed")
  private Boolean consumed;     // MATERIAL에서만 의미

  @Column(name="probability")
  private Double probability;   // PRODUCT에서만 의미

  public void setBlueprint(Blueprint blueprint) {
    this.blueprint = blueprint;
  }
}