// WalletJournalRepository.java
package spammy.eve.character.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.character.domain.WalletJournal;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface WalletJournalRepository extends JpaRepository<WalletJournal, Long>, WalletJournalRepositoryCustom {
    Set<Long> findJournalIdsByCharacterCharacterId(Long characterId);

    List<WalletJournal> getWalletJournalByJournalId(Long journalId);

    Optional<WalletJournal> findTopByCharacterCharacterIdOrderByJournalIdDesc(Long characterId);
}