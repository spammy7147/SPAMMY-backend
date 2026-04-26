package spammy.eve.sde;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "category")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Category {

    @Id
    @Column(name = "category_id", nullable = false)
    private Long id;

    @Column(name = "icon_id")
    private Long iconId;

    @Column(name = "name_en", nullable = false, length = 255)
    private String nameEn;

    @Column(name = "name_ko", length = 255)
    private String nameKo;

    @Column(name = "published", nullable = false)
    private boolean published = true;


}