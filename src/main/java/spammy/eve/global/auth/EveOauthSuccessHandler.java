package spammy.eve.global.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class EveOauthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;

    @Value("${spammy.frontend.url}")
    private String frontendUrl;

    @Value("${spammy.jwt.expiration}")
    private long jwtExpiration;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // 1. 커스텀 OAuth2User에서 userId 추출
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        Long userId = oAuth2User.getUserId();

        // 2. userId 기반 JWT 생성
        String accessToken = jwtService.generateToken(userId);

        // 3. JWT를 쿠키에 담기
        Cookie cookie = new Cookie("auth_token", accessToken);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 운영 환경에서는 true 필요
        cookie.setMaxAge((int) (jwtExpiration / 1000));
        response.addCookie(cookie);

        // 4. 연동(Link)용 임시 쿠키 제거
        Cookie userIdCookie = new Cookie("user_id", null);
        userIdCookie.setPath("/");
        userIdCookie.setMaxAge(0);
        response.addCookie(userIdCookie);

        // 5. 프론트엔드 메인 페이지로 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/");
    }
}