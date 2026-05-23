package com.su60.quickboot.common.security;

/**
 * 密码编码器接口
 *
 * @author luyanan
 * @since 2024/12/19
 */
public interface PasswordEncoder {

    /**
     * 编码密码
     *
     * @param rawPassword 原始密码
     * @return 编码后的密码
     */
    String encode(CharSequence rawPassword);

    /**
     * 验证密码是否匹配
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 编码后的密码
     * @return 是否匹配
     */
    boolean matches(CharSequence rawPassword, String encodedPassword);

    /**
     * 是否需要升级编码
     *
     * @param encodedPassword 编码后的密码
     * @return 是否需要升级
     */
    default boolean upgradeEncoding(String encodedPassword) {
        return false;
    }
}
