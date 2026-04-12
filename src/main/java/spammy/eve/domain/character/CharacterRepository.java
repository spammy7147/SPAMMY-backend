package spammy.eve.domain.character;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.domain.character.Character;
import spammy.eve.domain.user.User;

import java.util.List;
import java.util.Optional;

public interface CharacterRepository extends JpaRepository<Character, Long> {

    List<Character> findByUser(User user);

    Optional<Character> findByUserAndMainTrue(User user);

    boolean existsByUserAndMainTrue(User user);
}