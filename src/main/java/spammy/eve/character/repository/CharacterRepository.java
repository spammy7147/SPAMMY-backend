package spammy.eve.character.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.character.domain.Character;
import spammy.eve.character.domain.User;

import java.util.List;
import java.util.Optional;

public interface CharacterRepository extends JpaRepository<Character, Long>, CharacterRepositoryCustom {

    List<Character> findByUser(User user);

    Optional<Character> findByUserAndMainTrue(User user);

    Optional<Character> findByUser_IdAndMainTrue(Long userId);

    Optional<Character> findFirstByUser_Id(Long userId);

    List<Character> findByUser_Id(Long userId);

    boolean existsByUserAndMainTrue(User user);
}