package io.github.genkidoudou.common.desensitization;

/**
 * {@link Sensitive} 内置脱敏类型（仅对 {@link String} 序列化生效）。
 */
public enum SensitiveType {
    NAME,
    ID_CARD,
    MOBILE,
    BANK_CARD,
    EMAIL,
    ADDRESS,
    PASSWORD,
    /** 使用 {@link Sensitive#strategy()}，格式 {@code prefix,suffix}。 */
    CUSTOM
}
