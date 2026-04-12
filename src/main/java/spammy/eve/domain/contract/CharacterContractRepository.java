// CharacterContractRepository.java
package spammy.eve.domain.contract;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CharacterContractRepository extends JpaRepository<CharacterContract, Long> {
    List<CharacterContract> findByCharacterCharacterId(Long characterId);
}