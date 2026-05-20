package spammy.eve.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
public class JournalQueryDto {
    private OffsetDateTime date;
    private String refType;
    private Double amount;
    private Double balance;
    private String description;
    private Long firstPartyId;
    private String characterName;
}
