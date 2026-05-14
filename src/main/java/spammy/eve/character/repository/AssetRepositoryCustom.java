package spammy.eve.character.repository;

import spammy.eve.character.dto.AssetResponse;

public interface AssetRepositoryCustom {
    AssetResponse getAssets(Long userId);
}