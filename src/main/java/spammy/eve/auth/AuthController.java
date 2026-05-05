package spammy.eve.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spammy.eve.character.domain.Character;
import spammy.eve.character.repository.CharacterRepository;
import spammy.eve.user.User;
import spammy.eve.user.UserRepository;
import spammy.eve.global.auth.JwtService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;

    /**
     * 프론트엔드에서 토큰의 유효성을 검증하고, 현재 로그인된 사용자의 기본 정보를 조회합니다.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMe(@CookieValue(name = "auth_token", required = false) String authToken) {
        if (authToken == null) {
            return ResponseEntity.status(401).body("토큰이 존재하지 않습니다.");
        }

        try {
            Long userId = jwtService.getUserId(authToken);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            List<Character> chars = characterRepository.findByUser(user);
            
            // 응답으로 내려줄 캐릭터 목록 구성
            List<Map<String, Object>> characterList = chars.stream().map(c -> {
                Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("characterId", c.getCharacterId());
                m.put("characterName", c.getCharacterName());
                m.put("portraitUrl", c.getPortraitUrl());
                m.put("main", c.isMain());
                return m;
            }).toList();

            Map<String, Object> response = new java.util.LinkedHashMap<>();
            response.put("userId", user.getId());
            response.put("characters", characterList);

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("토큰 검증 실패: {}", e.getMessage());
            return ResponseEntity.status(401).body("유효하지 않거나 만료된 토큰입니다.");
        }
    }
}
