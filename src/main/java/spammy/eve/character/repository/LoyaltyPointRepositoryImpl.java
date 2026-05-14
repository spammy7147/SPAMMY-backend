package spammy.eve.character.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import spammy.eve.character.domain.QLoyaltyPoint;
import spammy.eve.character.domain.QCharacter;
import spammy.eve.character.dto.LpResponse;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class LoyaltyPointRepositoryImpl implements LoyaltyPointRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public LpResponse getLoyaltyPoints(Long userId) {
        QLoyaltyPoint lp = QLoyaltyPoint.loyaltyPoint;
        QCharacter character = QCharacter.character;

        List<Tuple> results = queryFactory
                .select(lp, character.characterName)
                .from(lp)
                .join(lp.character, character)
                .where(character.user.id.eq(userId))
                .fetch();

        Map<String, List<LpFlatData>> charMap = results.stream()
                .map(t -> new LpFlatData(
                        t.get(lp),
                        t.get(character.characterName)
                ))
                .collect(Collectors.groupingBy(LpFlatData::getCharName));

        List<LpResponse.CharacterLpGroup> charLps = new ArrayList<>();

        for (Map.Entry<String, List<LpFlatData>> entry : charMap.entrySet()) {
            String charName = entry.getKey();
            List<LpFlatData> dataList = entry.getValue();

            int totalLp = 0;
            double totalIskValue = 0;
            List<LpResponse.LpFactionGroup> factions = new ArrayList<>();

            for (LpFlatData data : dataList) {
                int lpValue = data.getLp().getLoyaltyPoints();
                double rate = 1000.0; // Placeholder: 1000 ISK per LP
                double value = lpValue * rate;

                totalLp += lpValue;
                totalIskValue += value;

                factions.add(LpResponse.LpFactionGroup.builder()
                        .name("Corp ID: " + data.getLp().getCorporationId())
                        .lp(lpValue)
                        .rate(rate)
                        .value(value)
                        .build());
            }

            charLps.add(LpResponse.CharacterLpGroup.builder()
                    .charName(charName)
                    .totalLp(totalLp)
                    .totalIskValue(totalIskValue)
                    .factions(factions)
                    .build());
        }

        return LpResponse.builder()
                .characterLps(charLps)
                .build();
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class LpFlatData {
        private spammy.eve.character.domain.LoyaltyPoint lp;
        private String charName;
    }
}