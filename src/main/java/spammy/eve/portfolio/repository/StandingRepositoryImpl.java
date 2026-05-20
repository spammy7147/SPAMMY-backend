package spammy.eve.portfolio.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import spammy.eve.portfolio.domain.QCharacter;
import spammy.eve.portfolio.domain.QStanding;
import spammy.eve.portfolio.response.StandingResponse;

import java.util.*;
import java.util.stream.Collectors;
import spammy.eve.portfolio.domain.User;

@RequiredArgsConstructor
public class StandingRepositoryImpl implements StandingRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public StandingResponse getStandings(User user) {
        QStanding standing = QStanding.standing;
        QCharacter character = QCharacter.character;

        List<Tuple> results = queryFactory
                .select(standing, character.characterName)
                .from(standing)
                .join(standing.character, character)
                .where(character.user.id.eq(user.getId()))
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