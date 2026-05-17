package spammy.eve.character.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.character.domain.Character;
import spammy.eve.character.domain.User;

import java.util.List;
import java.util.Optional;

public interface CharacterRepository extends JpaRepository<Character, Long>, CharacterRepositoryCustom {

    List<Character> findByUser(User user);

    Optional<Character> findFirstByUser(User user);

    Optional<Character> findByUserAndMainTrue(User user);

    boolean existsByUserAndMainTrue(User user);
}