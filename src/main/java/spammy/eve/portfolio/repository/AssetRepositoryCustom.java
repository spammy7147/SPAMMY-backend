package spammy.eve.portfolio.repository;

import spammy.eve.portfolio.domain.User;
import spammy.eve.portfolio.response.AssetResponse;

public interface AssetRepositoryCustom {
    AssetResponse getAssets(User user);
}