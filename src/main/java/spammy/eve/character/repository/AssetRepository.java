// AssetRepository.java
package spammy.eve.character.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.character.domain.Asset;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByCharacterCharacterId(Long characterId);
    void deleteByCharacterCharacterId(Long characterId);
}