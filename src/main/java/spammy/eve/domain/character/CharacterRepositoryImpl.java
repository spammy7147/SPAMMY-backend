package spammy.eve.domain.character;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CharacterRepositoryImpl implements CharactorRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Character> findByNameDynamic(Long characterId) {
        QCharacter character = QCharacter.character; // 자동 생성된 QClass

        return queryFactory
                .selectFrom(character)
                .where(characterId != null ? character.characterId.eq(characterId) : null)
                .fetch();
    }
}