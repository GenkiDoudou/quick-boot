package io.github.genkidoudou.common.api;

import cn.hutool.core.util.StrUtil;
import org.slf4j.MDC;

import java.util.regex.Pattern;

/**
 * 从 SLF4J {@link MDC} 读取 OAuth / Client HMAC 的 {@code clientId}（与 {@link ClientOperationIds} 分离）。
 * <p>
 * 值由 {@link io.github.genkidoudou.common.tracing.ClientOperationFilter} 从 HTTP Header
 * {@link #HEADER_NAME} 解析写入；用于登录日志、操作日志关联发起请求的客户端应用。
 */
public final class ClientIds {

  /** 与日志 pattern {@code %X{clientId}} 对齐的 MDC 键名。 */
  public static final String MDC_KEY = "clientId";

  /** 前端注入的请求头名。 */
  public static final String HEADER_NAME = "X-Client-Id";

  /** 最大合法长度（与 DB 列 VARCHAR(64) 一致）。 */
  public static final int MAX_LENGTH = 64;

  /** 允许字母、数字、连字符、下划线。 */
  private static final Pattern VALID = Pattern.compile("^[\\w\\-]+$");

  private ClientIds() {
  }

  /**
   * @return 非空白 clientId；否则 {@code null}
   */
  public static String current() {
    String v = MDC.get(MDC_KEY);
    if (v == null) {
      return null;
    }
    String t = v.trim();
    return t.isEmpty() ? null : t;
  }

  /**
   * 校验并规范化 Header 值；非法则返回 {@code null}。
   *
   * @param raw 原始 Header 值
   * @return 合法 clientId 或 {@code null}
   */
  public static String normalizeHeader(String raw) {
    if (StrUtil.isBlank(raw)) {
      return null;
    }
    String t = raw.trim();
    if (t.length() > MAX_LENGTH) {
      return null;
    }
    if (!VALID.matcher(t).matches()) {
      return null;
    }
    return t;
  }
}
