package spammy.eve.domain.character;

import java.util.List;

public interface CharactorRepositoryCustom {
    List<Character> findByNameDynamic(Long characterId);
}