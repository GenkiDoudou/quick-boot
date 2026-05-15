package io.github.genkidoudou.common.firewall.password;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SmUtil;
import io.github.genkidoudou.common.exception.ErrorException;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * sm4 加密算法
 *
 * @author luyanan
 * @since 2026/3/7
 */

public class Sm4PasswordEncoder implements PasswordEncoder {

    Properties properties = new Properties();
    @Override
    public String encrypt(CharSequence rawPassword) {

        String property = properties.getProperty("key");
        if (StrUtil.isBlank(property)) {
            throw new ErrorException(10004);
        }
        return SmUtil.sm4(property.getBytes(StandardCharsets.UTF_8)).encryptHex(rawPassword.toString());
    }

    @Override
    public String decrypt(String encodedPassword) {
        String property = properties.getProperty("key");
        if (StrUtil.isBlank(property)) {
            throw new ErrorException(10004);
        }
        return SmUtil.sm4(property.getBytes(StandardCharsets.UTF_8)).decryptStr(encodedPassword);
    }

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
