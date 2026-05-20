// IndustryJobRepository.java
package spammy.eve.portfolio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.portfolio.domain.IndustryJob;

import java.util.List;

public interface IndustryJobRepository extends JpaRepository<IndustryJob, Long> {
    List<IndustryJob> findByCharacterCharacterId(Long characterId);
    List<IndustryJob> findByCharacterCharacterIdAndStatus(Long characterId, String status);
}