package spammy.eve.portfolio.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class OrderResponse {
    private List<OrderEntry> entries;

    @Getter
    @Builder
    public static class OrderEntry {
        private Long orderId;
        private String charName;
        private String typeName;
        private Long typeId;
        private Double price;
        private Integer volumeRemain;
        private Integer volumeTotal;
        private Boolean isBuyOrder;
        private String locationName;
        private Instant issued;
        private String state;
    }
}
