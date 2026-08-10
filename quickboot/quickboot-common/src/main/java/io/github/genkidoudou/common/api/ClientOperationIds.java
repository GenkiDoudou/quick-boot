package io.github.genkidoudou.common.api;

import cn.hutool.core.util.StrUtil;
import org.slf4j.MDC;

import java.util.regex.Pattern;

/**
 * 从 SLF4J {@link MDC} 读取前端一次用户操作的 {@code operationId}（与 {@link TraceIds} 分离）。
 * <p>
 * 值由 {@link io.github.genkidoudou.common.tracing.ClientOperationFilter} 从 HTTP Header
 * {@link #HEADER_NAME} 解析写入；不参与 W3C trace 传播，也不写入 {@link TraceIds}。
 */
public final class ClientOperationIds {

  /** 与日志 pattern {@code %X{clientOperationId}} 对齐的 MDC 键名。 */
  public static final String MDC_KEY = "clientOperationId";

  /** 前端 axios 注入的请求头名。 */
  public static final String HEADER_NAME = "X-Client-Operation-Id";

  /** 最大合法长度（与 DB VARCHAR(64) 一致）。 */
  public static final int MAX_LENGTH = 64;

  /** 允许字母、数字、连字符、下划线（兼容 UUID 等）。 */
  private static final Pattern VALID = Pattern.compile("^[\\w\\-]+$");

  private ClientOperationIds() {
  }

  /**
   * @return 非空白 operationId；否则 {@code null}
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
   * @return 合法 operationId 或 {@code null}
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
