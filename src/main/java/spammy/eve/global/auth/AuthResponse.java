package spammy.eve.global.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private Long userId;
    private String characterName;
    private boolean authenticated;

    public static AuthResponse authenticated(Long userId, String characterName) {
        return AuthResponse.builder()
                .userId(userId)
                .characterName(characterName)
                .authenticated(true)
                .build();
    }

    public static AuthResponse unauthenticated() {
        return AuthResponse.builder()
                .authenticated(false)
                .build();
    }
}
