package spammy.eve.portfolio.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class TransactionResponse {
    private List<TransactionEntry> entries;
    private Map<String, TypeSummary> typeSummary;

    @Getter
    @Builder
    public static class TransactionEntry {
        private Long transactionId;
        private Instant date;
        private String charName;
        private String typeName;
        private Long typeId;
        private Integer quantity;
        private Double unitPrice;
        private Double totalPrice;
        private Boolean isBuy;
        private String clientName;
        private String locationName;
    }

    @Getter
    @Builder
    public static class TypeSummary {
        private Integer count;
        private Double totalVolume;
        private Double totalIsk;
    }
}
