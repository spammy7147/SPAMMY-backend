// LoyaltyPointRepository.java
package spammy.eve.domain.loyalty;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LoyaltyPointRepository extends JpaRepository<LoyaltyPoint, Long> {
    List<LoyaltyPoint> findByCharacterCharacterId(Long characterId);
    Optional<LoyaltyPoint> findByCharacterCharacterIdAndCorporationId(Long characterId, Long corporationId);
}