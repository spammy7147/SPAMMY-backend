package spammy.eve.global.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import spammy.eve.client.EsiResponse;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.IntStream;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class EsiCacheAspect {

    private final RedisTemplate<String, Object> redis;
    private final EsiMetadataRepository metadataRepository;

    @Around("@annotation(spammy.eve.global.aop.EsiCache)")
    @Transactional
    public Object esiCacheAspect(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();
        String[] parameterNames = signature.getParameterNames();

        String path = IntStream.range(0, parameterNames.length)
                .filter(i -> "path".equals(parameterNames[i]))
                .mapToObj(i -> (String) args[i])
                .findFirst()
                .orElse("");

        // 1. Redis 캐시 확인
        Object cachedValue = null;
        try {
            cachedValue = redis.opsForValue().get(path);
        } catch (Exception e) {
            log.warn("Redis 캐시 조회 실패: {}", e.getMessage());
        }

        if (cachedValue instanceof EsiMetadata) {
            log.debug("ESI Redis 캐시 히트: {}", path);
            return EsiResponse.builder().modified(false).build();
        }

        // 2. DB Metadata에서 ETag 조회 및 주입
        int etagIdx = IntStream.range(0, parameterNames.length)
                .filter(i -> "etag".equals(parameterNames[i]))
                .findFirst()
                .orElse(-1);

        if (etagIdx != -1 && args[etagIdx] == null) {
            String savedEtag = metadataRepository.findById(path)
                    .map(EsiMetadata::getEtag)
                    .orElse(null);
            args[etagIdx] = savedEtag;
        }

        // 3. ESI 호출
        Object result = joinPoint.proceed(args);
        if (!(result instanceof EsiResponse info)) return result;

        // 4. ESI 응답 처리 및 캐시 동기화
        HttpHeaders headers = info.getHeaders();
        Instant expires = parseExpires(headers);
        Duration ttl = Duration.between(Instant.now(), expires);

        if (info.isModified()) {
            // 200 OK: DB Metadata 업데이트
            String etag = headers.getFirst(HttpHeaders.ETAG);
            String lastModified = headers.getFirst(HttpHeaders.LAST_MODIFIED);
            metadataRepository.findById(path).ifPresentOrElse(
                    existing -> existing.update(etag, expires, lastModified),
                    () -> metadataRepository.save(EsiMetadata.of(path, etag, expires, lastModified))
            );
        }

        // 공통: Redis 캐싱 (TTL 동기화)
        if (ttl.isPositive()) {
            EsiMetadata redisMeta = EsiMetadata.builder()
                    .path(path)
                    .etag(headers.getFirst(HttpHeaders.ETAG))
                    .lastModified(headers.getFirst(HttpHeaders.LAST_MODIFIED))
                    .expires(expires)
                    .build();

            try {
                redis.opsForValue().set(path, redisMeta, ttl);
                log.debug("Redis 캐시 설정 완료: {} (TTL: {}s)", path, ttl.toSeconds());
            } catch (Exception e) {
                log.warn("Redis 캐시 저장 실패: {}", e.getMessage());
            }
        }

        return info;
    }

    private Instant parseExpires(HttpHeaders headers) {
        String expiresStr = headers.getFirst(HttpHeaders.EXPIRES);
        if (expiresStr == null) return Instant.now().plusSeconds(60);
        try {
            return Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(expiresStr));
        } catch (Exception e) {
            return Instant.now().plusSeconds(60);
        }
    }
}
       return Instant.now().plusSeconds(60);
        }
    }
}
