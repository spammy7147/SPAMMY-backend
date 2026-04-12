package spammy.eve.domain.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import spammy.eve.domain.character.Character;
import spammy.eve.domain.character.CharacterRepository;
import spammy.eve.domain.user.User;
import spammy.eve.domain.user.UserRepository;
import spammy.eve.global.auth.JwtService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final EsiProperties esiProperties;
    private final EsiAuthService esiAuthService;
    private final JwtService jwtService;
    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;

    @Value("${spammy.frontend.url}")
    private String frontendUrl;

    @Value("${spammy.jwt.expiration}")
    private long jwtExpiration;

    /**
     * EVE SSO 로그인 프로세스를 시작합니다.
     * 새로운 사용자가 로그인하거나, 기존 사용자가 다시 로그인할 때 사용됩니다.
     */
    @GetMapping("/login")
    public RedirectView login(HttpSession session) {
        String state = UUID.randomUUID().toString();
        session.setAttribute("oauth_state", state);
        // link_user_id가 없으면 일반 '로그인' 모드로 동작합니다.
        return new RedirectView(buildOAuthUrl(state));
    }

    /**
     * 현재 로그인된 계정에 새로운 EVE 캐릭터를 추가로 연결(Link)합니다.
     * 쿠키에 저장된 현재 사용자의 ID를 세션에 임시 저장하여 콜백 시 식별합니다.
     */
    @GetMapping("/link")
    public RedirectView link(HttpSession session, HttpServletRequest request) {
        Long userId = getUserIdFromCookieOrNull(request);
        if (userId == null) {
            return new RedirectView(frontendUrl + "/login?error=not_logged_in");
        }
        String state = UUID.randomUUID().toString();
        session.setAttribute("oauth_state", state);
        // 세션에 현재 사용자 ID를 저장하여 '연결' 모드임을 표시합니다.
        session.setAttribute("link_user_id", userId);
        return new RedirectView(buildOAuthUrl(state));
    }

    /**
     * EVE SSO 인증이 완료된 후 호출되는 공통 콜백 핸들러입니다.
     * 일반 로그인과 캐릭터 추가 연결(Link)을 모두 처리합니다.
     */
    @GetMapping("/callback")
    public RedirectView callback(@RequestParam String code, @RequestParam String state,
                                 HttpSession session, HttpServletResponse response) {
        // CSRF 방지를 위한 state 검증
        String savedState = (String) session.getAttribute("oauth_state");
        if (!state.equals(savedState)) {
            return new RedirectView(frontendUrl + "/login?error=state_mismatch");
        }

        // '연결' 모드인지 확인 후 세션에서 제거
        Long linkUserId = (Long) session.getAttribute("link_user_id");
        session.removeAttribute("link_user_id");

        try {
            // EsiAuthService에서 실제 토큰 교환 및 사용자/캐릭터 저장/병합 로직을 수행합니다.
            Character character = esiAuthService.handleCallback(code, linkUserId);

            if (linkUserId != null) {
                // [캐릭터 연결 모드]: 이미 로그인된 상태이므로 쿠키를 재발급하지 않고 대시보드로 복귀합니다.
                return new RedirectView(frontendUrl + "/dashboard?linked=" +
                        URLEncoder.encode(character.getCharacterName(), StandardCharsets.UTF_8));
            }

            // [일반 로그인 모드]: 새 JWT 토큰을 발급하여 HttpOnly 쿠키에 저장합니다.
            String token = jwtService.generateToken(character.getUser().getId());
            ResponseCookie cookie = ResponseCookie.from("auth_token", token)
                    .httpOnly(true)
                    .secure(false) // HTTPS 환경에서는 true로 변경 필요
                    .path("/")
                    .maxAge(jwtExpiration / 1000)
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            return new RedirectView(frontendUrl + "/dashboard");

        } catch (Exception e) {
            log.error("ESI 인증 실패", e);
            return new RedirectView(frontendUrl + "/login?error=auth_failed");
        }
    }

    /**
     * 현재 로그인한 사용자 그룹에 속한 모든 EVE 캐릭터 목록을 조회합니다.
     */
    @GetMapping("/characters")
    public ResponseEntity<?> getCharacters(HttpServletRequest request) {
        Long userId = getUserIdFromCookie(request);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User 없음: " + userId));
        
        List<Character> chars = characterRepository.findByUser(user);
        List<Map<String, Object>> dto = chars.stream().map(c -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("characterId", c.getCharacterId());
            m.put("characterName", c.getCharacterName());
            m.put("portraitUrl", c.getPortraitUrl());
            m.put("corporationId", c.getCorporationId());
            m.put("allianceId", c.getAllianceId());
            m.put("main", c.isMain());
            return m;
        }).toList();
        return ResponseEntity.ok(dto);
    }

    /**
     * 지정된 캐릭터를 해당 사용자 그룹의 '메인(대표) 캐릭터'로 설정합니다.
     * 기존에 설정된 메인 캐릭터는 일반 캐릭터로 강등됩니다.
     */
    @Transactional
    @PatchMapping("/characters/{characterId}/main")
    public ResponseEntity<?> setMain(@PathVariable Long characterId, HttpServletRequest request) {
        Long userId = getUserIdFromCookie(request);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User 없음: " + userId));

        List<Character> chars = characterRepository.findByUser(user);
        Character target = chars.stream()
                .filter(c -> c.getCharacterId().equals(characterId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("내 캐릭터가 아닙니다."));

        // 기존 메인 해제 후 새로운 메인 설정
        chars.forEach(c -> {
            if (c.isMain()) c.demoteFromMain();
        });
        target.setAsMain();
        characterRepository.saveAll(chars);

        return ResponseEntity.ok().build();
    }

    /**
     * 캐릭터를 현재 사용자 그룹에서 연결 해제(Unlink)합니다.
     * 그룹에 캐릭터가 하나뿐인 경우 해제가 불가능합니다.
     */
    @Transactional
    @DeleteMapping("/characters/{characterId}")
    public ResponseEntity<?> unlink(@PathVariable Long characterId, HttpServletRequest request) {
        Long userId = getUserIdFromCookie(request);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User 없음: " + userId));

        List<Character> chars = characterRepository.findByUser(user);
        if (chars.size() <= 1) {
            return ResponseEntity.badRequest().body("마지막 캐릭터는 해제할 수 없습니다.");
        }

        Character target = chars.stream()
                .filter(c -> c.getCharacterId().equals(characterId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("내 캐릭터가 아닙니다."));

        boolean wasMain = target.isMain();
        target.linkToUser(null); // 관계 끊기
        target.demoteFromMain();
        characterRepository.save(target);

        // 삭제된 캐릭터가 메인이었다면, 남은 캐릭터 중 하나를 메인으로 자동 지정합니다.
        if (wasMain) {
            chars.stream()
                    .filter(c -> !c.getCharacterId().equals(characterId))
                    .findFirst()
                    .ifPresent(c -> {
                        c.setAsMain();
                        characterRepository.save(c);
                    });
        }

        return ResponseEntity.ok().build();
    }

    /**
     * 로그아웃을 처리합니다. 브라우저의 auth_token 쿠키를 만료시킵니다.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("auth_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok().build();
    }

    /**
     * 쿠키에서 JWT를 추출하여 유효한 사용자 ID(User Primary Key)를 반환합니다.
     * 인증되지 않은 경우 예외를 발생시킵니다.
     */
    private Long getUserIdFromCookie(HttpServletRequest request) {
        Long userId = getUserIdFromCookieOrNull(request);
        if (userId == null) throw new RuntimeException("로그인 필요");
        return userId;
    }

    /**
     * 쿠키에서 JWT를 추출하여 사용자 ID를 반환하거나, 인증되지 않은 경우 null을 반환합니다.
     */
    private Long getUserIdFromCookieOrNull(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals("auth_token"))
                .findFirst()
                .map(Cookie::getValue)
                .map(token -> {
                    try { return jwtService.getUserId(token); }
                    catch (Exception e) { return null; }
                })
                .orElse(null);
    }

    /**
     * EVE Online SSO 인증 페이지로 리다이렉트하기 위한 URL을 생성합니다.
     */
    private String buildOAuthUrl(String state) {
        String scopes = URLEncoder.encode(
                esiProperties.getScopes().replace(",", " "),
                StandardCharsets.UTF_8
        );
        return "https://login.eveonline.com/v2/oauth/authorize" +
                "?response_type=code" +
                "&client_id=" + esiProperties.getClientId() +
                "&redirect_uri=" + URLEncoder.encode(esiProperties.getCallbackUrl(), StandardCharsets.UTF_8) +
                "&scope=" + scopes +
                "&state=" + state;
    }
}
