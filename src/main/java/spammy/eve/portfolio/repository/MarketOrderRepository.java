// MarketOrderRepository.java
package spammy.eve.portfolio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.portfolio.domain.MarketOrder;

import java.util.List;

public interface MarketOrderRepository extends JpaRepository<MarketOrder, Long>, MarketOrderRepositoryCustom {
    List<MarketOrder> findByCharacterCharacterId(Long characterId);
    List<MarketOrder> findByCharacterCharacterIdAndIsBuyOrder(Long characterId, Boolean isBuyOrder);
    void deleteByCharacterCharacterId(Long characterId);
}