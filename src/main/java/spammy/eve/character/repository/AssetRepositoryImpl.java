package spammy.eve.character.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import spammy.eve.character.domain.QAsset;
import spammy.eve.character.domain.QCharacter;
import spammy.eve.character.dto.AssetResponse;
import spammy.eve.market.QMarketPrice;
import spammy.eve.sde.QType;

import java.util.*;
import java.util.stream.Collectors;
import spammy.eve.character.domain.User;

@RequiredArgsConstructor
public class AssetRepositoryImpl implements AssetRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public AssetResponse getAssets(User user) {
        QAsset asset = QAsset.asset;
        QCharacter character = QCharacter.character;
        QType type = QType.type;
        QMarketPrice marketPrice = QMarketPrice.marketPrice;

        List<Tuple> results = queryFactory
                .select(asset, character.characterName, type.nameEn, marketPrice.averagePrice)
                .from(asset)
                .join(asset.character, character)
                .join(type).on(asset.typeId.eq(type.id))
                .leftJoin(marketPrice).on(asset.typeId.eq(marketPrice.typeId))
                .where(character.user.id.eq(user.getId()))
                .fetch();


        // 1. 캐릭터별로 그룹화
        Map<String, List<AssetFlatData>> charMap = results.stream()
                .map(t -> new AssetFlatData(
                        t.get(asset),
                        t.get(character.characterName),
                        t.get(type.nameEn),
                        t.get(marketPrice.averagePrice)
                ))
                .collect(Collectors.groupingBy(AssetFlatData::getCharacterName));

        List<AssetResponse.CharacterAssetGroup> charGroups = new ArrayList<>();

        for (Map.Entry<String, List<AssetFlatData>> charEntry : charMap.entrySet()) {
            String charName = charEntry.getKey();
            List<AssetFlatData> charAssets = charEntry.getValue();

            // 2. 위치별로 그룹화 (Root Location = locationType != "item")
            // locationType == "item" 인 것들은 컨테이너 내부 아이템임
            Map<Long, List<AssetFlatData>> locationMap = new HashMap<>();
            Map<Long, List<AssetFlatData>> containerContentMap = new HashMap<>();
            
            for (AssetFlatData d : charAssets) {
                if ("item".equals(d.getAsset().getLocationType())) {
                    containerContentMap.computeIfAbsent(d.getAsset().getLocationId(), k -> new ArrayList<>()).add(d);
                } else {
                    locationMap.computeIfAbsent(d.getAsset().getLocationId(), k -> new ArrayList<>()).add(d);
                }
            }

            List<AssetResponse.LocationGroup> locationGroups = new ArrayList<>();

            for (Map.Entry<Long, List<AssetFlatData>> locEntry : locationMap.entrySet()) {
                Long locId = locEntry.getKey();
                List<AssetFlatData> topLevelAssets = locEntry.getValue();

                List<AssetResponse.AssetItem> looseItems = new ArrayList<>();
                List<AssetResponse.ContainerGroup> containers = new ArrayList<>();
                double locTotalValue = 0;

                for (AssetFlatData topAsset : topLevelAssets) {
                    double price = topAsset.getAveragePrice() != null ? topAsset.getAveragePrice() : 0.0;
                    double value = price * topAsset.getAsset().getQuantity();
                    
                    List<AssetFlatData> contents = containerContentMap.get(topAsset.getAsset().getItemId());
                    if (contents != null && !contents.isEmpty()) {
                        // 컨테이너인 경우
                        List<AssetResponse.AssetItem> containerItems = new ArrayList<>();
                        double containerTotalValue = value; // 컨테이너 자체 가격 포함? 일단 포함
                        
                        for (AssetFlatData content : contents) {
                            double cPrice = content.getAveragePrice() != null ? content.getAveragePrice() : 0.0;
                            double cValue = cPrice * content.getAsset().getQuantity();
                            containerItems.add(AssetResponse.AssetItem.builder()
                                    .id(content.getAsset().getItemId())
                                    .name(content.getTypeName())
                                    .qty(content.getAsset().getQuantity())
                                    .value(cValue)
                                    .build());
                            containerTotalValue += cValue;
                        }
                        
                        containers.add(AssetResponse.ContainerGroup.builder()
                                .name(topAsset.getTypeName())
                                .totalValue(containerTotalValue)
                                .contents(containerItems)
                                .build());
                        locTotalValue += containerTotalValue;
                    } else {
                        // 일반 아이템인 경우
                        looseItems.add(AssetResponse.AssetItem.builder()
                                .id(topAsset.getAsset().getItemId())
                                .name(topAsset.getTypeName())
                                .qty(topAsset.getAsset().getQuantity())
                                .value(value)
                                .build());
                        locTotalValue += value;
                    }
                }

                locationGroups.add(AssetResponse.LocationGroup.builder()
                        .locationName("Location ID: " + locId) // 이름은 나중에 처리
                        .locationTotalValue(locTotalValue)
                        .items(looseItems)
                        .containers(containers)
                        .build());
            }

            charGroups.add(AssetResponse.CharacterAssetGroup.builder()
                    .characterName(charName)
                    .locations(locationGroups)
                    .build());
        }

        return AssetResponse.builder()
                .characterAssets(charGroups)
                .build();
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class AssetFlatData {
        private spammy.eve.character.domain.Asset asset;
        private String characterName;
        private String typeName;
        private Double averagePrice;
    }
}