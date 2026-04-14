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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

        RedisDTO dto = null;
        try {
            Object cachedValue = redis.opsForValue().get(path);
            if (cachedValue instanceof RedisDTO) {
                dto = (RedisDTO) cachedValue;
            }
        } catch (Exception e) {
            log.warn("ESI 캐시 데이터 읽기 실패 (무시하고 진행): {}", e.getMessage());
        }

        Object proceed = joinPoint.proceed();

        EsiResponse info = (EsiResponse) proceed;

        String lastModified = info.getHeaders().getFirst(HttpHeaders.LAST_MODIFIED);
        if(lastModified == null) {
            lastModified = ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        }
        long maxAge = 0;
        String cacheControl = info.getHeaders().getCacheControl();
        if (cacheControl != null && cacheControl.contains("max-age=")) {
            maxAge = Long.parseLong(cacheControl.split("max-age=")[1].split(",")[0].trim());
        }
        LocalDateTime expireAt = ZonedDateTime.parse(lastModified, DateTimeFormatter.RFC_1123_DATE_TIME).toLocalDateTime().plusSeconds(maxAge);

        redis.opsForValue().set(path, RedisDTO.builder()
                .etag(info.getHeaders().getFirst(HttpHeaders.ETAG))
                .expireAt(expireAt)
                .lastModifed(lastModified)
                .build());

        return proceed;
    }
}
