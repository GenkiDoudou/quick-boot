package io.github.genkidoudou.common.firewall.password;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 委托密码编码器
 * 支持多种密码编码算法，通过前缀标识使用的算法
 *
 * @author genkidoudou
 * @since 2026/03/05
 */
@Slf4j
public class DelegatingPasswordEncoder implements PasswordEncoder {

    private static final String PREFIX = "{";
    private static final String SUFFIX = "}";

    /**
     * 默认编码器ID
     */
    private final String idForEncode;

    /**
     * 默认编码器
     */
    private final PasswordEncoder passwordEncoderForEncode;

    /**
     * 编码器映射表
     */
    private final Map<String, PasswordEncoder> idToPasswordEncoder;

    /**
     * 构造委托密码编码器
     * 默认使用 bcrypt 算法
     *
     * @since 2026/03/05
     */
    public DelegatingPasswordEncoder() {
        this("bcrypt");
    }

    /**
     * 构造委托密码编码器
     *
     * @param idForEncode 默认编码器ID
     * @since 2026/03/05
     */
    public DelegatingPasswordEncoder(String idForEncode) {
        this.idForEncode = idForEncode;
        this.idToPasswordEncoder = new HashMap<>();

        // 注册默认编码器
        this.idToPasswordEncoder.put("bcrypt", new SpringBCryptPasswordEncoder());
        this.idToPasswordEncoder.put("sm4", new Sm4PasswordEncoder());

        this.passwordEncoderForEncode = this.idToPasswordEncoder.get(idForEncode);
        if (this.passwordEncoderForEncode == null) {
            throw new IllegalArgumentException("idForEncode " + idForEncode + " is not found in idToPasswordEncoder");
        }
    }

    /**
     * 注册密码编码器
     *
     * @param id              编码器ID
     * @param passwordEncoder 密码编码器
     * @since 2026/03/05
     */
    public void addPasswordEncoder(String id, PasswordEncoder passwordEncoder) {
        this.idToPasswordEncoder.put(id, passwordEncoder);
    }


    /**
     * 使用指定编码器加密
     *
     * @param rawPassword 明文密码
     * @param encoderId   编码器ID
     * @return 加密后的密码
     * @since 2026/03/05
     */
    public String encrypt(CharSequence rawPassword, String encoderId) {
        PasswordEncoder encoder = this.idToPasswordEncoder.get(encoderId);
        if (encoder == null) {
            throw new IllegalArgumentException("encoderId " + encoderId + " is not found in idToPasswordEncoder");
        }
        return PREFIX + encoderId + SUFFIX + encoder.encrypt(rawPassword);
    }

    public String decrypt(CharSequence rawPassword, String encoderId) {
        PasswordEncoder encoder = this.idToPasswordEncoder.get(encoderId);
        if (encoder == null) {
            throw new IllegalArgumentException("encoderId " + encoderId + " is not found in idToPasswordEncoder");
        }
        return PREFIX + encoderId + SUFFIX + encoder.decrypt(rawPassword.toString());
    }

    @Override
    public String encrypt(CharSequence rawPassword) {
        return PREFIX + this.idForEncode + SUFFIX + this.passwordEncoderForEncode.encrypt(rawPassword);
    }

    @Override
    public String decrypt(String encodedPassword) {
        return PREFIX + this.idForEncode + SUFFIX + this.passwordEncoderForEncode.decrypt(encodedPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String prefixEncodedPassword) {
        if (rawPassword == null && prefixEncodedPassword == null) {
            return true;
        }

        String id = extractId(prefixEncodedPassword);
        PasswordEncoder delegate = this.idToPasswordEncoder.get(id);

        if (delegate == null) {
            // 如果没有前缀，使用默认编码器
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
        }

        String encodedPassword = extractEncodedPassword(prefixEncodedPassword);
        return this.idToPasswordEncoder.get(id).upgradeEncoding(encodedPassword);
    }

    /**
     * 提取编码器ID
     *
     * @param prefixEncodedPassword 带前缀的加密密码
     * @return 编码器ID
     * @since 2026/03/05
     */
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

    /**
     * 提取加密密码
     *
     * @param prefixEncodedPassword 带前缀的加密密码
     * @return 加密密码
     * @since 2026/03/05
     */
    private String extractEncodedPassword(String prefixEncodedPassword) {
        int start = prefixEncodedPassword.indexOf(SUFFIX);
        return prefixEncodedPassword.substring(start + 1);
    }

    /**
     * 获取编码器
     *
     * @param id 编码器ID
     * @return 密码编码器
     * @since 2026/03/05
     */
    public PasswordEncoder getPasswordEncoder(String id) {
        return this.idToPasswordEncoder.get(id);
    }
}
