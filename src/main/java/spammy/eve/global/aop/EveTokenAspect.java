package spammy.eve.global.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import spammy.eve.portfolio.domain.Character;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class EveTokenAspect {

    private final EveTokenRefreshService tokenRefreshService;

    @Around("@annotation(spammy.eve.global.aop.EsiToken)")
    public Object checkToken(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        
        for (Object arg : args) {
            if (arg instanceof Character character) {
                tokenRefreshService.refreshTokenIfNeeded(character);
                break;
            }
        }
        
        return joinPoint.proceed();
    }
}
