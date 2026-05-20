// CharacterContractRepository.java
package spammy.eve.portfolio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.portfolio.domain.CharacterContract;

import java.util.List;

public interface CharacterContractRepository extends JpaRepository<CharacterContract, Long> {
    List<CharacterContract> findByCharacterCharacterId(Long characterId);
}