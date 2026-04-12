// AssetRepository.java
package spammy.eve.domain.asset;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.domain.Asset;
import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByCharacterCharacterId(Long characterId);
    void deleteByCharacterCharacterId(Long characterId);
}