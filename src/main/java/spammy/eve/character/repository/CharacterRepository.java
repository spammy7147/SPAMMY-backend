package spammy.eve.character.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.character.domain.Character;
import spammy.eve.user.User;

import java.util.List;
import java.util.Optional;

public interface CharacterRepository extends JpaRepository<spammy.eve.character.domain.Character, Long> {

    List<spammy.eve.character.domain.Character> findByUser(User user);

    Optional<Character> findByUserAndMainTrue(User user);

    boolean existsByUserAndMainTrue(User user);
}