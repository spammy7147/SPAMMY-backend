// WalletTransactionRepository.java
package spammy.eve.domain.wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.domain.WalletTransaction;
import java.util.Set;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    Set<Long> findTransactionIdsByCharacterCharacterId(Long characterId);
}