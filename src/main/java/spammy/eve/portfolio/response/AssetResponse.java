package spammy.eve.portfolio.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class AssetResponse {
    private List<CharacterAssetGroup> characterAssets;

    @Getter
    @Builder
    public static class CharacterAssetGroup {
        private String characterName;
        private List<LocationGroup> locations;
    }

    @Getter
    @Builder
    public static class LocationGroup {
        private String locationName;
        private Double locationTotalValue;
        private List<AssetItem> items;
        private List<ContainerGroup> containers;
    }

    @Getter
    @Builder
    public static class AssetItem {
        private Long id;
        private String name;
        private Integer qty;
        private Double value;
    }

    @Getter
    @Builder
    public static class ContainerGroup {
        private String name;
        private Double totalValue;
        private List<AssetItem> contents;
    }
}
