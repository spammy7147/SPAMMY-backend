package spammy.eve.domain.character;

import jakarta.persistence.*;
import lombok.*;
import spammy.eve.domain.user.User;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "characters")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Character {

    @Id
    @Column(name = "character_id")
    private Long characterId;

    @Column(name = "character_name", nullable = false)
    private String characterName;

    @Column(name = "access_token", nullable = false, length = 2048)
    private String accessToken;

    @Column(name = "refresh_token", nullable = false, length = 2048)
    private String refreshToken;

    @Column(name = "token_expires_at", nullable = false)
    private LocalDateTime tokenExpiresAt;

    @Column(name = "corporation_id")
    private Long corporationId;

    @Column(name = "corporation_name")
    private String corporationName;

    @Column(name = "alliance_id")
    private Long allianceId;

    @Column(name = "alliance_name")
    private String allianceName;

    @Column(name = "portrait_url", length = 512)
    private String portraitUrl;

    @Column(name = "scopes", length = 1024)
    private String scopes;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "balance")
    private Double balance;

    @Column(name = "omega_expires_at")
    private LocalDate omegaExpiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_main", nullable = false)
    @Builder.Default
    private boolean main = false;

    public boolean isTokenExpired() {
        return LocalDateTime.now().isAfter(tokenExpiresAt.minusSeconds(30));
    }

    public void updateToken(String accessToken, String refreshToken, LocalDateTime tokenExpiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public void updateAccessToken(String refreshToken, String accessToken, LocalDateTime tokenExpiresAt) {
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public void updateInfo(Long corporationId, Long allianceId) {
        this.corporationId = corporationId;
        this.allianceId = allianceId;
    }

    public void updatePortrait(String portraitUrl) {
        this.portraitUrl = portraitUrl;
    }

    public void updateBalance(Double balance) {
        this.balance = balance;
    }

    public void updateOmegaExpiresAt(LocalDate omegaExpiresAt) {
        this.omegaExpiresAt = omegaExpiresAt;
    }

    public void updateLastSyncedAt() {
        this.lastSyncedAt = LocalDateTime.now();
    }

    public void linkToUser(User user) {
        this.user = user;
    }

    public void setAsMain() {
        this.main = true;
    }

    public void demoteFromMain() {
        this.main = false;
    }

    public void updateCorpAndAlianceName(String corporationName, String allianceName) {
        this.corporationName = corporationName;
        this.allianceName = allianceName;
    }
}
