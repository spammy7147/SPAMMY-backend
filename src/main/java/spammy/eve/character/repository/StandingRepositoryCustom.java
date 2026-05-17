package spammy.eve.character.repository;

import spammy.eve.character.domain.User;
import spammy.eve.character.dto.StandingResponse;

public interface StandingRepositoryCustom {
    StandingResponse getStandings(User user);
}