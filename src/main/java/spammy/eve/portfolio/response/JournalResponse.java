package spammy.eve.portfolio.response;

import lombok.Builder;
import lombok.Getter;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class JournalResponse {
    private List<JournalEntry> entries;
    private Map<String, TypeSummary> typeSummary;

    @Getter
    @Builder
    public static class JournalEntry {
        private OffsetDateTime date;
        private String charName;
        private String type;
        private String desc;
        private Double amount;
        private Double balance;
    }

    @Getter
    @Builder
    public static class TypeSummary {
        private Integer count;
        private Double total;
    }
}
