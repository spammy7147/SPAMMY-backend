package spammy.eve.global.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import spammy.eve.client.EsiResponse;

import java.util.stream.IntStream;


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

        String paramname = IntStream.range(0, parameterNames.length)
                .filter(i -> "path".equals(parameterNames[i]))
                .mapToObj(i -> (String) args[i])
                .findFirst()
                .orElse("");

        String accessToken = IntStream.range(0, parameterNames.length)
                .filter(i -> "accessToken".equals(parameterNames[i]))
                .mapToObj(i -> (String) args[i])
                .findFirst()
                .orElse("");

        Object proceed = joinPoint.proceed();

        /**
         * TODO
         * 해더의 etag랑 expire date 저장
         */
        EsiResponse info = (EsiResponse) proceed;
        redis.opsForValue().set(paramname + accessToken, info);

        return proceed;
    }
}
