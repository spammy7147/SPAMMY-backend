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
import spammy.eve.client.EsiResponse;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.IntStream;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class EsiCacheAspect {

    private final RedisTemplate<String, Object> redis;

    @Around("@annotation(spammy.eve.global.aop.EsiCache)")
    public Object esiCacheAspect(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();
        String[] parameterNames = signature.getParameterNames();

        String path = IntStream.range(0, parameterNames.length)
                .filter(i -> "path".equals(parameterNames[i]))
                .mapToObj(i -> (String) args[i])
                .findFirst()
                .orElse("");

        Object cachedValue = null;

        try {
            cachedValue = redis.opsForValue().get(path);
        } catch (Exception e) {
            log.warn("ESI 캐시 조회 실패, DB 직접 조회 수행: {}", e.getMessage());
        }

        if (cachedValue instanceof RedisDTO) {
            return EsiResponse.builder()
                    .modified(false)
                    .build();
        }

        Object result = joinPoint.proceed();
        EsiResponse info = (EsiResponse) result;

        String lastModified = info.getHeaders().getFirst(HttpHeaders.LAST_MODIFIED);
        long maxAge = 60;
        String cacheControl = info.getHeaders().getCacheControl();
        if (cacheControl != null && cacheControl.contains("max-age=")) {
            maxAge = Arrays.stream(cacheControl.split(","))
                     .map(String::trim)
                     .filter(s -> s.startsWith("max-age="))
                     .map(s -> s.substring("max-age=".length()))
                     .mapToLong(s -> {
                             try { return Long.parseLong(s); }
                             catch (NumberFormatException e) { return 0L; }
                         })
                     .findFirst()
                     .orElse(0L);
        }

        redis.opsForValue().set(path,
                                RedisDTO.builder()
                                .etag(info.getHeaders().getFirst(HttpHeaders.ETAG))
                                .expireAt(LocalDateTime.now().plusSeconds(maxAge))
                                .lastModified(lastModified)
                                .build(),
                                Duration.ofSeconds(maxAge)
        );

        return info;
    }
}
