package spammy.eve.character.repository;

import spammy.eve.character.dto.JournalResponse;
import spammy.eve.character.dto.MissionResponse;

public interface WalletJournalRepositoryCustom {
    JournalResponse getJournal(Long userId);
    MissionResponse getMissions(Long userId);
}