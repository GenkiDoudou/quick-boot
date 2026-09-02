package io.github.genkidoudou.common.idempotency;

import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * 将 HTTP 请求、方法参数与 {@link Idempotent} 注解解析为服务端存储用的完整幂等键。
 * <p>
 * 优先级：{@link Idempotent#key()} SpEL → Header {@link IdempotencyKeys#HEADER_NAME} → 跳过或报错。
 */
public final class IdempotencyKeyResolver {

  private IdempotencyKeyResolver() {
  }

  /**
   * 解析完整存储键；无法解析且非必填时返回 {@code null}（跳过幂等校验）。
   *
   * @param request    当前请求
   * @param idempotent 方法注解
   * @param properties 全局配置
   * @param method     目标方法（SpEL 用）
   * @param args       方法实参（SpEL 用）
   * @return 完整存储键，或 {@code null}
   */
  public static String resolve(
      HttpServletRequest request,
      Idempotent idempotent,
      IdempotencyProperties properties,
      Method method,
      Object[] args) {
    String clientKey = resolveClientKey(request, idempotent, properties, method, args);
    if (clientKey == null) {
      return null;
    }
    return buildStorageKey(clientKey, idempotent, properties, request.getMethod(), request.getRequestURI());
  }

  /**
   * 解析客户端键片段：SpEL 优先，其次 Header。
   */
  static String resolveClientKey(
      HttpServletRequest request,
      Idempotent idempotent,
      IdempotencyProperties properties,
      Method method,
      Object[] args) {
    if (StringUtils.hasText(idempotent.key())) {
      return IdempotencySpelEvaluator.evaluate(idempotent.key(), method, args, request);
    }
    String headerName = StringUtils.hasText(properties.getHeaderName())
        ? properties.getHeaderName()
        : IdempotencyKeys.HEADER_NAME;
    String headerKey = IdempotencyKeys.normalizeHeader(request.getHeader(headerName));
    if (headerKey != null) {
      return headerKey;
    }
    if (idempotent.required()) {
      throw WarningException.literal(ErrorCodes.Common.IDEMPOTENCY_KEY_REQUIRED, "缺少或非法的幂等键 Header");
    }
    return null;
  }

  /**
   * 由客户端键与请求上下文组装服务端存储键（便于单测）。
   */
  static String buildStorageKey(
      String clientKey,
      Idempotent idempotent,
      IdempotencyProperties properties,
      String httpMethod,
      String requestUri) {
    StringBuilder key = new StringBuilder(properties.getKeyPrefix());
    if (idempotent.scope() == IdempotencyScope.USER) {
      key.append(resolveLoginId()).append(':');
    }
    if (idempotent.includeUri()) {
      key.append(httpMethod).append(':').append(requestUri).append(':');
    }
    key.append(clientKey);
    return key.toString();
  }

  private static String resolveLoginId() {
    try {
      if (cn.dev33.satoken.stp.StpUtil.isLogin()) {
        return cn.dev33.satoken.stp.StpUtil.getLoginIdAsString();
      }
    } catch (Throwable ignored) {
      // 测试或未装配 sa-token 时视为匿名
    }
    return "anon";
  }
}
