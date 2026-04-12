package spammy.eve.domain.contract;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "contract_item",
    indexes = {
        @Index(name = "idx_ci_contract_id", columnList = "contract_id"),
        @Index(name = "idx_ci_type_id", columnList = "type_id")
    }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContractItem {

    @Id
    @Column(name = "record_id")
    private Long recordId; // ESI record_id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private CharacterContract contract;

    @Column(name = "type_id", nullable = false)
    private Long typeId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "is_included", nullable = false)
    private Boolean isIncluded; // true=제공, false=요청

    @Column(name = "is_singleton")
    private Boolean isSingleton;

    @Column(name = "raw_quantity")
    private Integer rawQuantity; // BPC면 음수
}