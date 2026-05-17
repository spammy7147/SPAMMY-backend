package spammy.eve.character.repository;

import spammy.eve.character.domain.User;
import spammy.eve.character.dto.TransactionResponse;

public interface WalletTransactionRepositoryCustom {
    TransactionResponse getTransactions(User user);
}
