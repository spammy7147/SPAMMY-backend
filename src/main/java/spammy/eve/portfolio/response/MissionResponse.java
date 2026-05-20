package spammy.eve.portfolio.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class MissionResponse {
    private MissionStats stats;
    private List<DailyMissionRecord> dailyRecords;

    @Getter
    @Builder
    public static class MissionStats {
        private Integer totalCount;
        private Double totalIsk;
        private Integer totalLp;
        private Double totalLpValue;
    }

    @Getter
    @Builder
    public static class DailyMissionRecord {
        private String date;
        private Integer count;
        private Double iskIncome;
        private Integer lpEarned;
        private Double totalIncome;
    }
}
