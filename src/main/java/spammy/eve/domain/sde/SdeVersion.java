package spammy.eve.domain.sde;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "sde_version")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SdeVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "etag", nullable = false)
    private String etag;

    @Column(name = "last_modified")
    private String lastModified;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static SdeVersion of(String etag, String lastModified) {
        return SdeVersion.builder()
                .etag(etag)
                .lastModified(lastModified)
                .updatedAt(Instant.now())
                .build();
    }
}