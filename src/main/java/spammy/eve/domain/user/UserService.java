package spammy.eve.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import spammy.eve.domain.character.Character;
import spammy.eve.domain.character.CharacterRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;

    public Character check(Long characterId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 계정을 찾을 수 없습니다"));

        Character character = characterRepository.findById(characterId)
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("해당 캐릭터를 찾을 수 없거나 권한이 없습니다."));

        return character;
    }

}
