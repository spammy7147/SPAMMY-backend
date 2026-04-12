package spammy.eve.global.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${spammy.jwt.secret}")
    private String secret;

    @Value("${spammy.jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(Long userId) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key)
                .compact();
    }

    public Long getUserId(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Long.valueOf(
            Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject()
        );
    }
}