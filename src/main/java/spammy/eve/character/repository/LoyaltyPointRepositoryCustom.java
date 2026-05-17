package spammy.eve.character.repository;

import spammy.eve.character.domain.User;
import spammy.eve.character.dto.LpResponse;

public interface LoyaltyPointRepositoryCustom {
    LpResponse getLoyaltyPoints(User user);
}