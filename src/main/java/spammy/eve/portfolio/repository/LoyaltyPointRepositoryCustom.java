package spammy.eve.portfolio.repository;

import spammy.eve.portfolio.domain.User;
import spammy.eve.portfolio.response.LpResponse;

public interface LoyaltyPointRepositoryCustom {
    LpResponse getLoyaltyPoints(User user);
}