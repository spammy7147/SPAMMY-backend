package spammy.eve.character.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.character.domain.CharacterBlueprint;

import java.util.List;

public interface CharacterBlueprintRepository extends JpaRepository<CharacterBlueprint, Long> {
    List<CharacterBlueprint> findByCharacterCharacterId(Long characterId);
    void deleteByCharacterCharacterId(Long characterId);
}