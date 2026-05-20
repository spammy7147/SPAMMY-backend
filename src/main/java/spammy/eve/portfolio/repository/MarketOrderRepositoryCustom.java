package spammy.eve.portfolio.repository;

import spammy.eve.portfolio.domain.User;
import spammy.eve.portfolio.response.OrderResponse;

public interface MarketOrderRepositoryCustom {
    OrderResponse getOrders(User user);
}
