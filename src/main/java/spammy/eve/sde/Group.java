package spammy.eve.sde;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "`group`",   // ⚠ SQL 예약어 회피
    indexes = {
        @Index(name = "idx_group_category_id", columnList = "category_id")
    }
)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Group {

    @Id
    @Column(name = "group_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Category category;

    @Column(name = "name_en", nullable = false, length = 255)
    private String nameEn;

    @Column(name = "name_ko", length = 255)
    private String nameKo;

    @Column(name = "published", nullable = false)
    private boolean published = true;


}