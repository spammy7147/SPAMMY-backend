// LoyaltyPointRepository.java
package spammy.eve.character.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.character.domain.LoyaltyPoint;

import java.util.List;
import java.util.Optional;

public interface LoyaltyPointRepository extends JpaRepository<LoyaltyPoint, Long> {
    List<LoyaltyPoint> findByCharacterCharacterId(Long characterId);
    Optional<LoyaltyPoint> findByCharacterCharacterIdAndCorporationId(Long characterId, Long corporationId);
}