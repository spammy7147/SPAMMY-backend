// LoyaltyPointRepository.java
package spammy.eve.portfolio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.portfolio.domain.LoyaltyPoint;

import java.util.List;
import java.util.Optional;

public interface LoyaltyPointRepository extends JpaRepository<LoyaltyPoint, Long>, LoyaltyPointRepositoryCustom {
    List<LoyaltyPoint> findByCharacterCharacterId(Long characterId);
    Optional<LoyaltyPoint> findByCharacterCharacterIdAndCorporationId(Long characterId, Long corporationId);
}