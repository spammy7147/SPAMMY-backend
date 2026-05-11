package spammy.eve.character.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class LpResponse {
    private List<CharacterLpGroup> characterLps;

    @Getter
    @Builder
    public static class CharacterLpGroup {
        private String charName;
        private Integer totalLp;
        private Double totalIskValue;
        private List<LpFactionGroup> factions;
    }

    @Getter
    @Builder
    public static class LpFactionGroup {
        private String name;
        private Integer lp;
        private Double rate;
        private Double value;
    }
}
