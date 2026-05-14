package spammy.eve.character.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.character.domain.Standing;

import java.util.List;
import java.util.Optional;

public interface StandingRepository extends JpaRepository<Standing, Long>, StandingRepositoryCustom {
    List<Standing> findByCharacterCharacterId(Long characterId);
    Optional<Standing> findByCharacterCharacterIdAndFromIdAndFromType(Long characterId, Long fromId, String fromType);
}
