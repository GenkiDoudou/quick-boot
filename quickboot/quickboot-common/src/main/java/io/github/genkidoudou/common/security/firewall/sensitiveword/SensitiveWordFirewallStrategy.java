package io.github.genkidoudou.common.security.firewall.sensitiveword;

/**
 * 敏感词处理策略：原地替换或直接拒绝请求。
 */
public enum SensitiveWordFirewallStrategy {

    /** 使用 houbb 默认替换策略掩码命中词。 */
    REPLACE,

    /** 命中敏感词时抛出 {@link SensitiveWordException}，由 Filter 写出 {@code R} JSON。 */
    THROW
}
