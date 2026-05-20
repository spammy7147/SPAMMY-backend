package spammy.eve.portfolio.repository;

import spammy.eve.portfolio.domain.User;
import spammy.eve.portfolio.response.StandingResponse;

public interface StandingRepositoryCustom {
    StandingResponse getStandings(User user);
}