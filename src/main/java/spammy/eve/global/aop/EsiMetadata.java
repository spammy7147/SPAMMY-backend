package spammy.eve.global.aop;

import jakarta.persistence.*;
import lombok.*;
import spammy.eve.global.domain.BaseEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "esi_metadata")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EsiMetadata extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "path", length = 512)
    private String path;

    @Column(name = "etag")
    private String etag;

    @Column(name = "last_modified")
    private String lastModified;

    @Column(name = "expires")
    private Instant expires;

    public static EsiMetadata of(String path, String etag, Instant expires, String lastModified) {
        return EsiMetadata.builder()
                .path(path)
                .etag(etag)
                .expires(expires)
                .lastModified(lastModified)
                .build();
    }

    /**
     * ESI 메타데이터 정보를 갱신합니다.
     */
    public void update(String etag, Instant expires, String lastModified) {
        this.etag = etag;
        this.expires = expires;
        this.lastModified = lastModified;
    }
}
