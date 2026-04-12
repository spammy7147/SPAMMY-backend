// MarketOrderRepository.java
package spammy.eve.domain.market;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MarketOrderRepository extends JpaRepository<MarketOrder, Long> {
    List<MarketOrder> findByCharacterCharacterId(Long characterId);
    List<MarketOrder> findByCharacterCharacterIdAndIsBuyOrder(Long characterId, Boolean isBuyOrder);
    void deleteByCharacterCharacterId(Long characterId);
}