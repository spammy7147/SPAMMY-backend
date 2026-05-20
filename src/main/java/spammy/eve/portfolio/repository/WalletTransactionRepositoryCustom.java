package spammy.eve.portfolio.repository;

import spammy.eve.portfolio.domain.User;
import spammy.eve.portfolio.response.TransactionResponse;

public interface WalletTransactionRepositoryCustom {
    TransactionResponse getTransactions(User user);
}
