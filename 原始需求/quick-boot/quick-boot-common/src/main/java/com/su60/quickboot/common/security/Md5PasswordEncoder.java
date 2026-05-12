package com.su60.quickboot.common.security;

import cn.hutool.crypto.digest.DigestUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * MD5密码编码器
 *
 * @author luyanan
 * @since 2024/12/19
 */
@Slf4j
public class Md5PasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword cannot be null");
        }
        return DigestUtil.md5Hex(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword cannot be null");
        }
        if (encodedPassword == null || encodedPassword.length() == 0) {
            log.warn("Empty encoded password");
            return false;
        }
        String encoded = encode(rawPassword);
        return encoded.equals(encodedPassword);
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return false;
    }
}
