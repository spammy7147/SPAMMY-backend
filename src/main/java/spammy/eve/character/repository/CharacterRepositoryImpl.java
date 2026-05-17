package spammy.eve.character.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import spammy.eve.character.domain.Character;
import spammy.eve.character.domain.QCharacter;
import spammy.eve.character.dto.SummaryResponse;

import java.util.List;
import java.util.stream.Collectors;
import spammy.eve.character.domain.User;

@RequiredArgsConstructor
public class CharacterRepositoryImpl implements CharacterRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Character> findByNameDynamic(Long characterId) {
        QCharacter character  = QCharacter.character; // 자동 생성된 QClass

        return queryFactory
                .selectFrom(character)
                .where(characterId != null ? character.characterId.eq(characterId) : null)
                .fetch();
    }

    @Override
    public SummaryResponse getSummary(User user) {
        QCharacter character = QCharacter.character;

        List<Character> characters = queryFactory
                .selectFrom(character)
                .where(character.user.id.eq(user.getId()))
                .fetch();


        double totalBalance = characters.stream()
                .mapToDouble(c -> c.getBalance() != null ? c.getBalance() : 0.0)
                .sum();

        List<SummaryResponse.CharacterSummary> characterSummaries = characters.stream()
                .map(c -> SummaryResponse.CharacterSummary.builder()
                        .characterId(c.getCharacterId())
                        .characterName(c.getCharacterName())
                        .corporationName(c.getCorporationName())
                        .allianceName(c.getAllianceName())
                        .balance(c.getBalance())
                        .omegaExpiresAt(c.getOmegaExpiresAt())
                        .lastSyncedAt(c.getLastSyncedAt())
                        .isMain(c.isMain())
                        .portraitUrl(c.getPortraitUrl())
                        .build())
                .collect(Collectors.toList());

        return SummaryResponse.builder()
                .totalBalance(totalBalance)
                .characters(characterSummaries)
                .build();
    }
}