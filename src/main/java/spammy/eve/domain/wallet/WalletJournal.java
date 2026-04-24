package spammy.eve.domain;

import jakarta.persistence.*;
import lombok.*;
import spammy.eve.domain.character.Character;

import java.time.Instant;
import java.time.OffsetDateTime;

@Entity
@Table(
    name = "wallet_journal",
    indexes = {
        @Index(name = "idx_wj_character_id", columnList = "character_id"),
        @Index(name = "idx_wj_date", columnList = "date"),
        @Index(name = "idx_wj_ref_type", columnList = "ref_type")
    }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WalletJournal {

    @Id
    @Column(name = "journal_id")
    private Long journalId; // ESI journal ID (고유값)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Character character;

    @Column(name = "date", nullable = false)
    private OffsetDateTime date;

    @Column(name = "ref_type", nullable = false, length = 100)
    private String refType; // bounty_prizes, brokers_fee 등

    @Column(name = "amount")
    private Double amount; // + 수입 / - 지출

    @Column(name = "balance")
    private Double balance; // 거래 후 잔액

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "first_party_id")
    private Long firstPartyId;

    @Column(name = "second_party_id")
    private Long secondPartyId;

    @Column(name = "tax")
    private Double tax;

    @Column(name = "tax_receiver_id")
    private Long taxReceiverId;

    @Column(name = "context_id")
    private Long contextId;

    @Column(name = "context_id_type", length = 100)
    private String contextIdType;
}