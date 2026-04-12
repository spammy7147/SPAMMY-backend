package spammy.eve.domain.blueprint;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CharacterBlueprintRepository extends JpaRepository<CharacterBlueprint, Long> {
    List<CharacterBlueprint> findByCharacterCharacterId(Long characterId);
    void deleteByCharacterCharacterId(Long characterId);
}