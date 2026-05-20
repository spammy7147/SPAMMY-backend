package spammy.eve.portfolio.domain;

import jakarta.persistence.*;
import lombok.*;
import spammy.eve.global.domain.BaseEntity;

@Entity
@Table(name = "user_settings")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSettings extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @Column(name = "isk_abbreviation", nullable = false)
    @Builder.Default
    private boolean iskAbbreviation = true;

    @Column(name = "timezone", nullable = false)
    @Builder.Default
    private String timezone = "UTC";

    public void updateIskAbbreviation(boolean iskAbbreviation) {
        this.iskAbbreviation = iskAbbreviation;
    }

    public void updateTimezone(String timezone) {
        this.timezone = timezone;
    }
}
