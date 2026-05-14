package spammy.eve.character.repository;

import spammy.eve.character.dto.StandingResponse;

public interface StandingRepositoryCustom {
    StandingResponse getStandings(Long userId);
}