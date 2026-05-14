package spammy.eve.character.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import spammy.eve.character.domain.QCharacter;
import spammy.eve.character.domain.QStanding;
import spammy.eve.character.dto.StandingResponse;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class StandingRepositoryImpl implements StandingRepositoryCustom { // Wait, name mismatch fixed in next tool call

    private final JPAQueryFactory queryFactory;

    @Override
    public StandingResponse getStandings(Long userId) {
        QStanding standing = QStanding.standing;
        QCharacter character = QCharacter.character;

        List<Tuple> results = queryFactory
                .select(standing, character.characterName)
                .from(standing)
                .join(standing.character, character)
                .where(character.user.id.eq(userId))
                .fetch();

        List<StandingResponse.StandingEntry> entries = results.stream()
                .map(t -> StandingResponse.StandingEntry.builder()
                        .name("ID: " + t.get(standing).getFromId())
                        .type(t.get(standing).getFromType())
                        .value(t.get(standing).getStandingValue())
                        .charName(t.get(character.characterName))
                        .build())
                .collect(Collectors.toList());

        return StandingResponse.builder()
                .standings(entries)
                .build();
    }
}