package io.github.genkidoudou.common.web;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 遗留 HTTP 接口兼容：为 {@code @Deprecated} 别名响应打上 {@code Deprecation: true} 头。
 */
public final class DeprecatedApiSupport {

  /** RFC 标准弃用响应头名。 */
  public static final String HEADER_NAME = "Deprecation";

  /** 弃用标记值。 */
  public static final String HEADER_VALUE = "true";

  private DeprecatedApiSupport() {
  }

  /**
   * 标记当前响应为弃用别名（兼容窗口内仍可用）。
   *
   * @param response HTTP 响应；为 null 时不操作
   */
  public static void markDeprecated(HttpServletResponse response) {
    if (response != null) {
      response.setHeader(HEADER_NAME, HEADER_VALUE);
    }
  }
}
