package spammy.eve.character.repository;

import spammy.eve.character.domain.User;
import spammy.eve.character.dto.OrderResponse;

public interface MarketOrderRepositoryCustom {
    OrderResponse getOrders(User user);
}
