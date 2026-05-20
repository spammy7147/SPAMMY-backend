package spammy.eve.portfolio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.portfolio.domain.LoyaltyPointHistory;

import java.util.List;

public interface LoyaltyPointHistoryRepository extends JpaRepository<LoyaltyPointHistory, Long> {
    List<LoyaltyPointHistory> findByCharacterCharacterId(Long characterId);
    List<LoyaltyPointHistory> findByCharacterCharacterIdAndCorporationId(Long characterId, Long corporationId);
    List<LoyaltyPointHistory> findByCharacterUserIdOrderByCreatedAtAsc(Long userId);
}
