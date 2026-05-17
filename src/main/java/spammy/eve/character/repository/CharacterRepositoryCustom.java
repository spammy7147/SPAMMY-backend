package spammy.eve.character.repository;

import spammy.eve.character.domain.Character;
import spammy.eve.character.domain.User;
import spammy.eve.character.dto.SummaryResponse;

import java.util.List;

public interface CharacterRepositoryCustom {
    List<Character> findByNameDynamic(Long characterId);
    SummaryResponse getSummary(User user);
}