package io.github.genkidoudou.common.firewall.password;

import java.util.Map;
import java.util.Properties;

/**
 * 密码编码器接口
 * 定义密码加密、解密和匹配的标准方法
 *
 * @author genkidoudou
 * @since 2026/03/05
 */
public interface PasswordEncoder {

    /**
     * 加密明文密码
     *
     * @param rawPassword 明文密码
     * @return 加密后的密码
     * @since 2026/03/05
     */
    String encrypt(CharSequence rawPassword);


    /**
     * 解密
     *
     * @param encodedPassword 密文密码
     * @return
     * @since 2026/3/7
     */
    String decrypt(String encodedPassword);

    /**
     * 验证明文密码是否与加密密码匹配
     *
     * @param rawPassword     明文密码
     * @param encodedPassword 加密密码
     * @return 是否匹配
     * @since 2026/03/05
     */
    default boolean matches(CharSequence rawPassword, String encodedPassword) {
        return rawPassword.equals(encodedPassword);
    }

    /**
     * 判断加密密码是否需要升级
     * 用于密码策略升级时的兼容处理
     *
     * @param encodedPassword 加密密码
     * @return 是否需要升级
     * @since 2026/03/05
     */
    default boolean upgradeEncoding(String encodedPassword) {
        return false;
    }


    default void setProperties(Properties properties) {
    }
}
