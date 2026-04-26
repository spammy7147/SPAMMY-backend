// CharacterContractRepository.java
package spammy.eve.character.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.character.domain.CharacterContract;

import java.util.List;

public interface CharacterContractRepository extends JpaRepository<CharacterContract, Long> {
    List<CharacterContract> findByCharacterCharacterId(Long characterId);
}