package spammy.eve.character.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
    name = "wallet_transaction",
    indexes = {
        @Index(name = "idx_wt_character_id", columnList = "character_id"),
        @Index(name = "idx_wt_date", columnList = "date"),
        @Index(name = "idx_wt_type_id", columnList = "type_id")
    }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WalletTransaction {

    @Id
    @Column(name = "transaction_id")
    private Long transactionId; // ESI transaction ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Character character;

    @Column(name = "date", nullable = false)
    private Instant date;

    @Column(name = "type_id", nullable = false)
    private Long typeId; // 아이템 type_id

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    @Column(name = "is_buy", nullable = false)
    private Boolean isBuy; // true=구매, false=판매

    @Column(name = "is_personal", nullable = false)
    private Boolean isPersonal;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "journal_ref_id")
    private Long journalRefId;
}