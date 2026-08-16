package io.github.genkidoudou.common.desensitization;

/**
 * {@link Sensitive} 内置脱敏类型（仅对 {@link String} 序列化生效）。
 */
public enum SensitiveType {
    /** 姓名：保留首字。 */
    NAME,
    /** 身份证号：前 6 后 4。 */
    ID_CARD,
    /** 手机号：前 3 后 4。 */
    MOBILE,
    /** 银行卡号：前 4 后 4。 */
    BANK_CARD,
    /** 邮箱：本地段前 2 位。 */
    EMAIL,
    /** 地址：前 6 位。 */
    ADDRESS,
    /** 密码：固定掩码。 */
    PASSWORD,
    /** 使用 {@link Sensitive#strategy()}，格式 {@code prefix,suffix}。 */
    CUSTOM
}
