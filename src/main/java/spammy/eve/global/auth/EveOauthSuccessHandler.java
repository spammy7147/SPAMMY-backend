package spammy.eve.global.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class EveOauthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${spammy.frontend.url}")
    private String frontendUrl;

    @Value("${spammy.jwt.expiration}")
    private long jwtExpiration;

    private final JwtTokenProvider tokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // 1. 커스텀 OAuth2User에서 userId 추출
        if (!(authentication.getPrincipal() instanceof CustomOAuth2User oAuth2User)) {
            log.error("Authentication principal is not CustomOAuth2User: {}", authentication.getPrincipal().getClass());
            getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/login?error=invalid_user_type");
            return;
        }

        Long userId = oAuth2User.getUserId();

        // 2. userId가 null인 경우 예외 처리
        if (userId == null) {
            log.error("Authentication success but userId is null for character: {}", oAuth2User.getName());
            getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/login?error=user_not_found");
            return;
        }

        // 3. userId 기반 JWT 생성
        String accessToken = tokenProvider.generateToken(userId);

        // 4. JWT를 쿠키에 담기
        Cookie cookie = new Cookie("auth_token", accessToken);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 운영 환경에서는 true 필요
        cookie.setMaxAge((int) (jwtExpiration / 1000));
        response.addCookie(cookie);

        // 5. 프론트엔드 메인 페이지로 리다이렉트
        log.info("User {} (character: {}) login success", userId, oAuth2User.getName());
        getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/");
    }
}