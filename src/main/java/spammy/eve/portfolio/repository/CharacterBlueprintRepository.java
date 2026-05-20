package spammy.eve.portfolio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.portfolio.domain.CharacterBlueprint;

import java.util.List;

public interface CharacterBlueprintRepository extends JpaRepository<CharacterBlueprint, Long> {
    List<CharacterBlueprint> findByCharacterCharacterId(Long characterId);
    void deleteByCharacterCharacterId(Long characterId);
}