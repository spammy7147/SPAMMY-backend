package spammy.eve.character.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class StandingResponse {
    private List<StandingEntry> standings;

    @Getter
    @Builder
    public static class StandingEntry {
        private String name;
        private String type;
        private Double value;
        private String charName;
    }
}
