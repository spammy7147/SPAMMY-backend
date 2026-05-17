package spammy.eve.character.repository;

import spammy.eve.character.domain.User;
import spammy.eve.character.dto.JournalResponse;
import spammy.eve.character.dto.MissionResponse;

public interface WalletJournalRepositoryCustom {
    JournalResponse getJournal(User user);
    MissionResponse getMissions(User user);
}