package spammy.eve.global.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import spammy.eve.global.auth.JwtService;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Value("${spammy.jwt.expiration}")
    private long jwtExpiration;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        log.debug("JwtFilter processing request: {} {}", request.getMethod(), request.getRequestURI());

        // 1. /api/** 경로에 대해서만 필터 적용
        if (!request.getRequestURI().startsWith("/api")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 쿠키에서 auth_token 추출
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            log.debug("[JwtFilter] Total cookies found: {}", cookies.length);
            Arrays.stream(cookies)
                    .filter(c -> c.getName().equals("auth_token"))
                    .findFirst()
                    .ifPresentOrElse(cookie -> {
                        try {
                            String token = cookie.getValue();
                            Long userId = jwtService.getUserId(token);
                            log.debug("[JwtFilter] Valid JWT for User ID: {}", userId);

                            // 3. [Sliding Expiration] 검증 성공 시 새 토큰 발급 및 쿠키 갱신
                            String newToken = jwtService.generateToken(userId);
                            ResponseCookie newCookie = ResponseCookie.from("auth_token", newToken)
                                    .httpOnly(true)
                                    .secure(false) 
                                    .path("/")
                                    .maxAge(jwtExpiration / 1000)
                                    .sameSite("Lax") // 이미지와 동일하게 Lax로 복구 (안정성)
                                    .build();
                            response.addHeader(HttpHeaders.SET_COOKIE, newCookie.toString());
                        } catch (Exception e) {
                            log.error("[JwtFilter] JWT validation failed: {}", e.getMessage());
                            ResponseCookie expiredCookie = ResponseCookie.from("auth_token", "")
                                    .httpOnly(true)
                                    .path("/")
                                    .maxAge(0)
                                    .build();
                            response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
                        }
                    }, () -> log.warn("[JwtFilter] 'auth_token' cookie NOT found in request among {} cookies", cookies.length));
        } else {
            log.warn("[JwtFilter] NO cookies received at all in request to {}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }
}
