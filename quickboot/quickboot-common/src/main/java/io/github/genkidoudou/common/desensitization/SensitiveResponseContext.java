package io.github.genkidoudou.common.desensitization;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

/**
 * 判断当前请求是否启用字段脱敏（是否命中 {@link SensitiveResponse}）。
 * <p>
 * 单元测试可通过 {@link #enableForTest()} / {@link #clear()} 绕过 Servlet 上下文。
 */
public final class SensitiveResponseContext {

  private static final ThreadLocal<Boolean> TEST_FORCE = new ThreadLocal<>();

  private SensitiveResponseContext() {
  }

  /** 测试：强制本线程启用脱敏。 */
  public static void enableForTest() {
    TEST_FORCE.set(Boolean.TRUE);
  }

  /** 测试：强制本线程关闭脱敏。 */
  public static void disableForTest() {
    TEST_FORCE.set(Boolean.FALSE);
  }

  /** 清除测试强制标记。 */
  public static void clear() {
    TEST_FORCE.remove();
  }

  /**
   * @return 当前是否应对 {@link Sensitive} 字段做掩码
   */
  public static boolean isActive() {
    Boolean forced = TEST_FORCE.get();
    if (forced != null) {
      return forced;
    }
    try {
      RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
      if (attrs == null) {
        return false;
      }
      Object handler = attrs.getAttribute(
          HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
      if (!(handler instanceof HandlerMethod hm)) {
        return false;
      }
      return hm.hasMethodAnnotation(SensitiveResponse.class)
          || hm.getBeanType().isAnnotationPresent(SensitiveResponse.class);
    } catch (IllegalStateException e) {
      return false;
    }
  }
}
