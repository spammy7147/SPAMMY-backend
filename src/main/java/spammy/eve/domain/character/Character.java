package spammy.eve.domain.character;

import jakarta.persistence.*;
import lombok.*;
import spammy.eve.domain.user.User;

import java.time.Instant;

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

    @Column(name = "owner_hash", nullable = false, length = 128)
    private String ownerHash;

    @Column(name = "access_token", nullable = false, length = 2048)
    private String accessToken;

    @Column(name = "refresh_token", nullable = false, length = 2048)
    private String refreshToken;

    @Column(name = "token_expires_at", nullable = false)
    private Instant tokenExpiresAt;

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
    private Instant lastSyncedAt;

    @Column(name = "balance")
    private Double balance;

    @Column(name = "omega_expires_at")
    private Instant omegaExpiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_main", nullable = false)
    @Builder.Default
    private boolean main = false;

    public boolean isTokenExpired() {
        return Instant.now().isAfter(tokenExpiresAt.minusSeconds(30));
    }

    public void updateToken(String accessToken, String refreshToken, Instant tokenExpiresAt, String scopes, String ownerHash) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = tokenExpiresAt;
        this.scopes = scopes;
        this.ownerHash = ownerHash;
    }

    public void updateAccessToken(String accessToken, Instant tokenExpiresAt) {
        this.accessToken = accessToken;
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public void updateInfo(String characterName, Long corporationId, String corporationName, Long allianceId, String allianceName, String portraitUrl) {
        this.characterName = characterName;
        this.corporationId = corporationId;
        this.allianceId = allianceId;
        this.portraitUrl = portraitUrl;
        this.allianceName = allianceName;
         this.corporationName = corporationName;
    }

    public void updateBalance(Double balance) {
        this.balance = balance;
    }

    public void updateOmegaExpiresAt(Instant omegaExpiresAt) {
        this.omegaExpiresAt = omegaExpiresAt;
    }

    public void updateLastSyncedAt() {
        this.lastSyncedAt = Instant.now();
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
}
