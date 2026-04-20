package spammy.eve.domain.character;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import spammy.eve.domain.character.Character;
import spammy.eve.domain.user.User;
import spammy.eve.domain.character.CharacterRepository;
import spammy.eve.domain.user.UserRepository;
import spammy.eve.global.auth.JwtService;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
public class CharacterController {

    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    /**
     * 캐릭터의 오메가 만료 시간을 수동으로 업데이트합니다.
     */
    @Transactional
    @PatchMapping("/{characterId}/omega")
    public ResponseEntity<?> updateOmega(@PathVariable Long characterId, 
                                        @RequestBody Map<String, String> body,
                                        HttpServletRequest request) {
        Long userId = getUserIdFromCookie(request);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User 없음: " + userId));

        Character character = characterRepository.findById(characterId)
                .filter(c -> c.getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("해당 캐릭터를 찾을 수 없거나 권한이 없습니다."));

        String dateStr = body.get("omegaExpiresAt");
        if (dateStr == null) {
            character.updateOmegaExpiresAt(null);
        } else {
            character.updateOmegaExpiresAt(Instant.parse(dateStr));
        }
        
        characterRepository.save(character);
        return ResponseEntity.ok().build();
    }

    private Long getUserIdFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) throw new RuntimeException("로그인 필요");
        return Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals("auth_token"))
                .findFirst()
                .map(Cookie::getValue)
                .map(token -> {
                    try { return jwtService.getUserId(token); }
                    catch (Exception e) { return null; }
                })
                .orElseThrow(() -> new RuntimeException("로그인 필요"));
    }

    //캐릭터정보 조회
    
}
