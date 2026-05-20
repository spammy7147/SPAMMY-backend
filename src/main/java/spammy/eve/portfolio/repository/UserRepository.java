package spammy.eve.portfolio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.portfolio.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
