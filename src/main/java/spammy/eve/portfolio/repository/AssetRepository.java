// AssetRepository.java
package spammy.eve.portfolio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.portfolio.domain.Asset;

import java.util.Collection;
import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long>, AssetRepositoryCustom {
    List<Asset> findByCharacterCharacterId(Long characterId);
    void deleteByCharacterCharacterId(Long characterId);
    void deleteByCharacterCharacterIdAndItemIdNotIn(Long characterId, Collection<Long> itemIds);
}