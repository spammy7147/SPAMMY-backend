// WalletJournalRepository.java
package spammy.eve.portfolio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.portfolio.domain.WalletJournal;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface WalletJournalRepository extends JpaRepository<WalletJournal, Long>, WalletJournalRepositoryCustom {
    Set<Long> findJournalIdsByCharacterCharacterId(Long characterId);

    List<WalletJournal> getWalletJournalByJournalId(Long journalId);

    Optional<WalletJournal> findTopByCharacterCharacterIdOrderByJournalIdDesc(Long characterId);
}