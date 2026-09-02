package io.github.genkidoudou.common.idempotency;

import cn.hutool.core.util.StrUtil;

import java.util.regex.Pattern;

/**
 * 幂等键 HTTP Header 常量与规范化工具。
 * <p>
 * 命名对齐 Stripe / RFC 惯用 {@code Idempotency-Key}；与 {@link io.github.genkidoudou.common.api.ClientOperationIds}
 * 解耦——后者用于链路追踪，本类用于写操作防重复。
 */
public final class IdempotencyKeys {

  /** 客户端应携带的幂等键请求头名。 */
  public static final String HEADER_NAME = "Idempotency-Key";

  /** 合法幂等键最大长度。 */
  public static final int MAX_LENGTH = 128;

  /** 允许字母、数字、连字符、下划线、点（兼容 UUID 等）。 */
  private static final Pattern VALID = Pattern.compile("^[\\w\\-.]+$");

  private IdempotencyKeys() {
  }

  /**
   * 校验并规范化 Header 值；非法则返回 {@code null}。
   *
   * @param raw 原始 Header 值
   * @return 合法幂等键或 {@code null}
   */
  public static String normalizeHeader(String raw) {
    if (StrUtil.isBlank(raw)) {
      return null;
    }
    String trimmed = raw.trim();
    if (trimmed.length() > MAX_LENGTH) {
      return null;
    }
    if (!VALID.matcher(trimmed).matches()) {
      return null;
    }
    return trimmed;
  }
}
