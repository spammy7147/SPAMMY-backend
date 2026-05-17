package spammy.eve.global.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import spammy.eve.character.domain.Character;
import spammy.eve.character.repository.CharacterRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class EveOauthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${spammy.frontend.url}")
    private String frontendUrl;

    @Value("${spammy.jwt.expiration}")
    private long jwtExpiration;

    private final JwtTokenProvider tokenProvider;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final CharacterRepository characterRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        log.info("EVE SSO 인증 성공 핸들러 시작: {}", authentication.getName());

        // 1. 커스텀 OAuth2User에서 userId 추출
        if (!(authentication.getPrincipal() instanceof CustomOAuth2User oAuth2User)) {
            log.error("Authentication principal is not CustomOAuth2User: {}", authentication.getPrincipal().getClass());
            getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/login?error=invalid_user_type");
            return;
        }

        Long userId = oAuth2User.getUserId();
        log.info("인증된 사용자 userId: {}, characterName: {}", userId, oAuth2User.getName());

        // 2. Refresh Token 확보 및 Character 엔티티 업데이트
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName());

            if (client != null) {
                OAuth2AccessToken accessToken = client.getAccessToken();
                OAuth2RefreshToken refreshToken = client.getRefreshToken();
                
                log.info("OAuth2 Client 확보 성공: hasRefreshToken={}", refreshToken != null);

                Long characterId = Long.valueOf(oAuth2User.getAttributes().get("CharacterID").toString());
                Character character = characterRepository.findById(characterId).orElse(null);

                if (character != null) {
                    String rtValue = refreshToken != null ? refreshToken.getTokenValue() : character.getRefreshToken();
                    LocalDateTime expiresAt = accessToken.getExpiresAt() != null 
                        ? LocalDateTime.ofInstant(accessToken.getExpiresAt(), ZoneId.systemDefault()) 
                        : LocalDateTime.now().plusMinutes(20);
                        
                    character.updateToken(accessToken.getTokenValue(), rtValue, expiresAt);
                    characterRepository.save(character);
                    log.info("캐릭터 토큰 정보 업데이트 완료 (SuccessHandler): characterName={}", character.getCharacterName());
                }
            } else {
                log.warn("OAuth2AuthorizedClient를 찾을 수 없습니다.");
            }
        }

        // 3. userId가 null인 경우 예외 처리
        if (userId == null) {
            log.error("Authentication success but userId is null for character: {}", oAuth2User.getName());
            getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/login?error=user_not_found");
            return;
        }

        // 4. userId 기반 JWT 생성
        String accessToken = tokenProvider.generateToken(userId);
        log.debug("JWT 생성 완료: userId={}", userId);

        // 5. JWT를 쿠키에 담기
        Cookie cookie = new Cookie("auth_token", accessToken);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 운영 환경에서는 true 필요
        cookie.setMaxAge((int) (jwtExpiration / 1000));
        response.addCookie(cookie);

        // 6. 프론트엔드 메인 페이지로 리다이렉트
        String targetUrl = frontendUrl + "/";
        log.info("로그인 최종 성공. 리다이렉트 대상: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}