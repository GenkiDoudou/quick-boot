package com.su60.quickboot.common.security;

import cn.hutool.crypto.digest.BCrypt;
import lombok.extern.slf4j.Slf4j;

/**
 * BCrypt密码编码器
 *
 * @author luyanan
 * @since 2024/12/19
 */
@Slf4j
public class BCryptPasswordEncoder implements PasswordEncoder {

    private final int strength;

    public BCryptPasswordEncoder() {
        this(-1);
    }

    public BCryptPasswordEncoder(int strength) {
        this.strength = strength;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword cannot be null");
        }
        String salt;
        if (strength > 0) {
            salt = BCrypt.gensalt(strength);
        } else {
            salt = BCrypt.gensalt();
        }
        return BCrypt.hashpw(rawPassword.toString(), salt);
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
        if (!BCrypt.checkpw(rawPassword.toString(), encodedPassword)) {
            log.warn("Password does not match stored value");
            return false;
        }
        return true;
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.length() == 0) {
            return false;
        }
        // Hutool的BCrypt没有getRounds方法，简化实现
        return strength > 0 && !encodedPassword.startsWith("$2a$" + strength + "$");
    }
}
