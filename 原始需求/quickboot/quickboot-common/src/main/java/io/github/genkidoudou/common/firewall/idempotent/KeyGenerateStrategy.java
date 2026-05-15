package io.github.genkidoudou.common.firewall.idempotent;

/**
 * 幂等键生成策略
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
public enum KeyGenerateStrategy {

    /**
     * 默认策略：方法签名 + 所有参数的 hashCode
     */
    DEFAULT,

    /**
     * 请求路径 + 所有参数的 hashCode
     */
    URL,

    /**
     * 请求路径 + 用户标识 + 所有参数的 hashCode
     */
    URL_USER,

    /**
     * Token 策略：从请求头中获取幂等 Token
     */
    TOKEN,

    /**
     * 自定义策略：使用自定义的键生成器
     */
    CUSTOM
}
