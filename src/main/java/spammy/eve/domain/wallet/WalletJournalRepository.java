// WalletJournalRepository.java
package spammy.eve.domain.wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.domain.WalletJournal;

import java.util.List;
import java.util.Set;

public interface WalletJournalRepository extends JpaRepository<WalletJournal, Long> {
    Set<Long> findJournalIdsByCharacterCharacterId(Long characterId);

    List<WalletJournal> getWalletJournalByJournalId(Long journalId);
}