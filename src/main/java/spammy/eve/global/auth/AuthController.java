package spammy.eve.global.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spammy.eve.portfolio.domain.Character;
import spammy.eve.portfolio.domain.User;
import spammy.eve.portfolio.repository.CharacterRepository;
import spammy.eve.portfolio.repository.UserRepository;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;

    /**
     * 로그아웃을 처리합니다. 쿠키를 삭제하고 인증 컨텍스트를 비웁니다.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        log.info("로그아웃 요청 - 쿠키 삭제 및 컨텍스트 초기화");

        Cookie cookie = new Cookie("auth_token", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 운영 환경에서는 true 권장
        response.addCookie(cookie);

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok().build();
    }

    /**
     * 현재 로그인된 사용자의 정보를 반환합니다.
     * JwtFilter를 통해 인증된 경우 SecurityContext에서 User 객체를 가져옵니다.
     */
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(@AuthenticationPrincipal User user) {
        log.info("로그인검증 api/auth/me user = {}", user);
        if (user == null) {
            return ResponseEntity.ok(AuthResponse.unauthenticated());
        }

        // 메인 캐릭터 정보 가져오기 (없으면 첫 번째 캐릭터)
        String characterName = characterRepository.findByUserAndMainTrue(user)
                .map(Character::getCharacterName)
                .orElseGet(() -> characterRepository.findFirstByUser(user)
                        .map(Character::getCharacterName)
                        .orElse(""));

        return ResponseEntity.ok(AuthResponse.authenticated(user.getId(), characterName));
    }
}

