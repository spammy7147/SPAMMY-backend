package spammy.eve.portfolio.repository;

import spammy.eve.portfolio.domain.User;
import spammy.eve.portfolio.response.JournalResponse;
import spammy.eve.portfolio.response.MissionResponse;

public interface WalletJournalRepositoryCustom {
    JournalResponse getJournal(User user);
    MissionResponse getMissions(User user);
}