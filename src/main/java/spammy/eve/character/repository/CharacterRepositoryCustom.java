package spammy.eve.character.repository;

import spammy.eve.character.domain.Character;

import java.util.List;

public interface CharacterRepositoryCustom {
    List<Character> findByNameDynamic(Long characterId);
}