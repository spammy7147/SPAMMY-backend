package spammy.eve.portfolio.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class SummaryResponse {
    private Double totalBalance;
    private List<CharacterSummary> characters;

    @Getter
    @Builder
    public static class CharacterSummary {
        private Long characterId;
        private String characterName;
        private String corporationName;
        private String allianceName;
        private Double balance;
        private LocalDateTime omegaExpiresAt;
        private LocalDateTime lastSyncedAt;
        private boolean isMain;
        private String portraitUrl;
    }
}
