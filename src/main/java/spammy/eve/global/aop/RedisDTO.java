package spammy.eve.global.aop;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RedisDTO {
    private String etag;
    private String lastModified;
    private LocalDateTime expireAt;
}
