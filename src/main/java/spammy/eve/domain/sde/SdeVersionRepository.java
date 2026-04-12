package spammy.eve.domain.sde;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SdeVersionRepository extends JpaRepository<SdeVersion, Long> {
    Optional<SdeVersion> findTopByOrderByUpdatedAtDesc();
}