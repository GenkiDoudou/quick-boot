package com.su60.quickboot.common.security;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 委托密码编码器
 *
 * @author luyanan
 * @since 2024/12/19
 */
@Slf4j
public class DelegatingPasswordEncoder implements PasswordEncoder {

    private static final String PREFIX = "{";
    private static final String SUFFIX = "}";
    private static final String NOOP_PREFIX = "{noop}";
    private static final String BCRYPT_PREFIX = "{bcrypt}";
    private static final String MD5_PREFIX = "{md5}";

    private final String idForEncode;
    private final PasswordEncoder passwordEncoderForEncode;
    private final Map<String, PasswordEncoder> idToPasswordEncoder;

    public DelegatingPasswordEncoder() {
        this.idForEncode = "bcrypt";
        this.passwordEncoderForEncode = new BCryptPasswordEncoder();
        this.idToPasswordEncoder = new HashMap<>();
        this.idToPasswordEncoder.put("bcrypt", new BCryptPasswordEncoder());
        this.idToPasswordEncoder.put("md5", new Md5PasswordEncoder());
        this.idToPasswordEncoder.put("noop", new NoOpPasswordEncoder());
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return PREFIX + this.idForEncode + SUFFIX + this.passwordEncoderForEncode.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String prefixEncodedPassword) {
        if (rawPassword == null && prefixEncodedPassword == null) {
            return true;
        }
        String id = extractId(prefixEncodedPassword);
        PasswordEncoder delegate = this.idToPasswordEncoder.get(id);
        if (delegate == null) {
            return this.passwordEncoderForEncode.matches(rawPassword, prefixEncodedPassword);
        }
        String encodedPassword = extractEncodedPassword(prefixEncodedPassword);
        return delegate.matches(rawPassword, encodedPassword);
    }

    @Override
    public boolean upgradeEncoding(String prefixEncodedPassword) {
        String id = extractId(prefixEncodedPassword);
        if (!this.idForEncode.equals(id)) {
            return true;
        } else {
            String encodedPassword = extractEncodedPassword(prefixEncodedPassword);
            return this.idToPasswordEncoder.get(id).upgradeEncoding(encodedPassword);
        }
    }

    private String extractId(String prefixEncodedPassword) {
        if (prefixEncodedPassword == null) {
            return null;
        }
        int start = prefixEncodedPassword.indexOf(PREFIX);
        if (start != 0) {
            return null;
        }
        int end = prefixEncodedPassword.indexOf(SUFFIX, start);
        if (end < 0) {
            return null;
        }
        return prefixEncodedPassword.substring(start + 1, end);
    }

    private String extractEncodedPassword(String prefixEncodedPassword) {
        int start = prefixEncodedPassword.indexOf(SUFFIX);
        return prefixEncodedPassword.substring(start + 1);
    }
}
