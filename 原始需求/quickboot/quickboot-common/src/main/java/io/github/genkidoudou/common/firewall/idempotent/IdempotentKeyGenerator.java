package io.github.genkidoudou.common.firewall.idempotent;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 幂等键生成器接口
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
public interface IdempotentKeyGenerator {

    /**
     * 生成幂等键
     *
     * @param joinPoint  切点
     * @param idempotent 幂等注解
     * @return 幂等键
     */
    String generateKey(ProceedingJoinPoint joinPoint, Idempotent idempotent);
}
