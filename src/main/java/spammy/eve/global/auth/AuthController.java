package spammy.eve.global.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spammy.eve.character.domain.Character;
import spammy.eve.character.repository.CharacterRepository;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CharacterRepository characterRepository;

    /**
     * 현재 로그인된 사용자의 정보를 반환합니다.
     * JwtFilter를 통해 인증된 경우 SecurityContext에서 userId를 가져옵니다.
     */
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(@AuthenticationPrincipal Long userId) {
        log.info("로그인검증 api/auth/me userId = {}", userId);
        if (userId == null) {
            return ResponseEntity.ok(AuthResponse.unauthenticated());
        }

        // 메인 캐릭터 정보 가져오기 (없으면 첫 번째 캐릭터)
        String characterName = characterRepository.findByUser_IdAndMainTrue(userId)
                .map(Character::getCharacterName)
                .orElseGet(() -> characterRepository.findFirstByUser_Id(userId)
                        .map(Character::getCharacterName)
                        .orElse("Unknown"));

        return ResponseEntity.ok(AuthResponse.authenticated(userId, characterName));
    }
}
