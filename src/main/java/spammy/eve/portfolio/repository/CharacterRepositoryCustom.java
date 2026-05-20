package spammy.eve.portfolio.repository;

import spammy.eve.portfolio.domain.Character;
import spammy.eve.portfolio.domain.User;
import spammy.eve.portfolio.response.SummaryResponse;

import java.util.List;

public interface CharacterRepositoryCustom {
    List<Character> findByNameDynamic(Long characterId);
    SummaryResponse getSummary(User user);
}