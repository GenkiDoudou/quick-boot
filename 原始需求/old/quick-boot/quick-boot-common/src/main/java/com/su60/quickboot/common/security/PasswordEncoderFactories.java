package com.su60.quickboot.common.security;

import lombok.extern.slf4j.Slf4j;

/**
 * 密码编码器工厂
 *
 * @author luyanan
 * @since 2024/12/19
 */
@Slf4j
public class PasswordEncoderFactories {

    /**
     * 创建委托密码编码器
     *
     * @return 委托密码编码器
     */
    public static PasswordEncoder createDelegatingPasswordEncoder() {
        return new DelegatingPasswordEncoder();
    }

    /**
     * 创建BCrypt密码编码器
     *
     * @return BCrypt密码编码器
     */
    public static PasswordEncoder createBCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 创建MD5密码编码器
     *
     * @return MD5密码编码器
     */
    public static PasswordEncoder createMd5PasswordEncoder() {
        return new Md5PasswordEncoder();
    }

    /**
     * 创建无操作密码编码器
     *
     * @return 无操作密码编码器
     */
    public static PasswordEncoder createNoOpPasswordEncoder() {
        return new NoOpPasswordEncoder();
    }
}
