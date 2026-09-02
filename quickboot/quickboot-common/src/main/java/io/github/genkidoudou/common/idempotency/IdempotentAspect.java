package io.github.genkidoudou.common.idempotency;

import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * 环绕 {@link Idempotent} 标注的方法：占用幂等键 → 执行业务 → 失败时释放占用。
 */
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
@RequiredArgsConstructor
public class IdempotentAspect {

  private final IdempotencyStore idempotencyStore;
  private final IdempotencyProperties properties;

  /**
   * @param joinPoint  连接点
   * @param idempotent 方法上的幂等注解
   * @return 原方法返回值
   * @throws Throwable 原方法异常；重复请求时抛出 {@link WarningException}
   */
  @Around("@annotation(idempotent)")
  public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
    if (!properties.isEnabled()) {
      return joinPoint.proceed();
    }
    HttpServletRequest request = currentRequest();
    if (request == null) {
      return joinPoint.proceed();
    }
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    Object[] args = joinPoint.getArgs();
    String key = IdempotencyKeyResolver.resolve(request, idempotent, properties, method, args);
    if (key == null) {
      return joinPoint.proceed();
    }
    Duration ttl = idempotent.ttlSeconds() > 0
        ? Duration.ofSeconds(idempotent.ttlSeconds())
        : Duration.ofSeconds(Math.max(1, properties.getTtlSeconds()));
    if (!idempotencyStore.tryAcquire(key, ttl)) {
      String msg = StringUtils.hasText(idempotent.message())
          ? idempotent.message()
          : "重复请求，请勿重复提交";
      throw WarningException.literal(ErrorCodes.Common.DUPLICATE_REQUEST, msg);
    }
    try {
      return joinPoint.proceed();
    } catch (Throwable ex) {
      idempotencyStore.release(key);
      throw ex;
    }
  }

  private static HttpServletRequest currentRequest() {
    if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
      return null;
    }
    return attrs.getRequest();
  }
}
