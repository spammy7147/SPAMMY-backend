package spammy.eve.portfolio.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsDto {
    private Long userId;
    private String mainCharacterName;
    private boolean iskAbbreviation;
    private String timezone;
}
