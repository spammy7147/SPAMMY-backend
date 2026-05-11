package spammy.eve.character.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.character.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
