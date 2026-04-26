// MarketOrderRepository.java
package spammy.eve.character.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.character.domain.MarketOrder;

import java.util.List;

public interface MarketOrderRepository extends JpaRepository<MarketOrder, Long> {
    List<MarketOrder> findByCharacterCharacterId(Long characterId);
    List<MarketOrder> findByCharacterCharacterIdAndIsBuyOrder(Long characterId, Boolean isBuyOrder);
    void deleteByCharacterCharacterId(Long characterId);
}