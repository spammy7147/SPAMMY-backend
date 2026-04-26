// WalletTransactionRepository.java
package spammy.eve.character.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.character.domain.WalletTransaction;

import java.util.Set;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    Set<Long> findTransactionIdsByCharacterCharacterId(Long characterId);
}