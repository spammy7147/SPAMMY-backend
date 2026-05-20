// WalletTransactionRepository.java
package spammy.eve.portfolio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.portfolio.domain.WalletTransaction;

import java.util.Optional;
import java.util.Set;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long>, WalletTransactionRepositoryCustom {
    Set<Long> findTransactionIdsByCharacterCharacterId(Long characterId);

    Optional<WalletTransaction> findTopByCharacterCharacterIdOrderByTransactionIdDesc(Long characterId);
}