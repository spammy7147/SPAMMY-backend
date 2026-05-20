package spammy.eve.portfolio.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import spammy.eve.portfolio.domain.QCharacter;
import spammy.eve.portfolio.domain.QWalletTransaction;
import spammy.eve.portfolio.response.TransactionResponse;
import spammy.eve.sde.QType;

import java.util.*;
import spammy.eve.portfolio.domain.User;

@RequiredArgsConstructor
public class WalletTransactionRepositoryImpl implements WalletTransactionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public TransactionResponse getTransactions(User user) {
        QWalletTransaction transaction = QWalletTransaction.walletTransaction;
        QCharacter character = QCharacter.character;
        QType type = QType.type;

        List<Tuple> results = queryFactory
                .select(transaction, character.characterName, type.nameEn)
                .from(transaction)
                .join(transaction.character, character)
                .leftJoin(type).on(transaction.typeId.eq(type.id))
                .where(character.user.id.eq(user.getId()))
                .orderBy(transaction.date.desc())
                .limit(1000)
                .fetch();


        List<TransactionResponse.TransactionEntry> entries = new ArrayList<>();
        Map<String, TransactionResponse.TypeSummary> summaryMap = new HashMap<>();

        for (Tuple t : results) {
            spammy.eve.portfolio.domain.WalletTransaction wt = t.get(transaction);
            String charName = t.get(character.characterName);
            String typeName = t.get(type.nameEn) != null ? t.get(type.nameEn) : "Unknown Item (" + wt.getTypeId() + ")";

            double totalPrice = wt.getUnitPrice() * wt.getQuantity();

            entries.add(TransactionResponse.TransactionEntry.builder()
                    .transactionId(wt.getTransactionId())
                    .date(wt.getDate())
                    .charName(charName)
                    .typeName(typeName)
                    .typeId(wt.getTypeId())
                    .quantity(wt.getQuantity())
                    .unitPrice(wt.getUnitPrice())
                    .totalPrice(totalPrice)
                    .isBuy(wt.getIsBuy())
                    .clientName("Client ID: " + wt.getClientId())
                    .locationName("Location ID: " + wt.getLocationId())
                    .build());

            TransactionResponse.TypeSummary summary = summaryMap.computeIfAbsent(typeName,
                    k -> TransactionResponse.TypeSummary.builder()
                            .count(0)
                            .totalVolume(0.0)
                            .totalIsk(0.0)
                            .build());

            summaryMap.put(typeName, TransactionResponse.TypeSummary.builder()
                    .count(summary.getCount() + 1)
                    .totalVolume(summary.getTotalVolume() + wt.getQuantity())
                    .totalIsk(summary.getTotalIsk() + (wt.getIsBuy() ? -totalPrice : totalPrice))
                    .build());
        }

        return TransactionResponse.builder()
                .entries(entries)
                .typeSummary(summaryMap)
                .build();
    }
}
