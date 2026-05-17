package spammy.eve.global.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ESI API 호출 전 토큰 유효성을 체크하고 필요 시 갱신하도록 표시하는 어노테이션입니다.
 * 메서드의 파라미터 중 spammy.eve.character.domain.Character 객체가 있어야 합니다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EsiToken {
}
