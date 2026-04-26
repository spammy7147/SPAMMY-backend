package spammy.eve.market;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
    name = "market_price",
    indexes = {
        @Index(name = "idx_mp_type_id", columnList = "type_id")
    }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MarketPrice {

    @Id
    @Column(name = "type_id")
    private Long typeId;

    @Column(name = "average_price")
    private Double averagePrice; // 시장 평균가

    @Column(name = "adjusted_price")
    private Double adjustedPrice; // 산업 세금(시스템 비용 지수) 계산 기준값

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void update(Double averagePrice, Double adjustedPrice) {
        this.averagePrice = averagePrice;
        this.adjustedPrice = adjustedPrice;
        this.updatedAt = Instant.now();
    }
}