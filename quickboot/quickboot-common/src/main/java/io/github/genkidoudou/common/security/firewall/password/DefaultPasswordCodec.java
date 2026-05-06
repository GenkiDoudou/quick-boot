package io.github.genkidoudou.common.security.firewall.password;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.crypto.symmetric.SM4;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * 基于 Hutool 的 {@link PasswordCodec} 默认实现：bcrypt 与 SM4（CBC/PKCS5Padding，密文负载为 Hutool 生成的 IV||ciphertext 整体的十六进制）。
 * <p>
 * SM4 工作模式与 Hutool {@link SM4} 一致，保证 encrypt / decrypt / matches 闭环；请勿手工构造十六进制负载。
 * </p>
 */
public class DefaultPasswordCodec implements PasswordCodec {

    private static final String PREFIX_BEGIN = "{";
    private static final String SM4_PREFIX = "sm4:";
    private static final Pattern HEX_KEY_PATTERN = Pattern.compile("(?i)[0-9a-f]{32}");

    private final Object lock = new Object();
    private volatile Map<String, byte[]> sm4Keys = Collections.emptyMap();
    private volatile String sm4DefaultKeyId;

    @Override
    public void setProperties(Properties properties) {
        if (properties == null) {
            synchronized (lock) {
                sm4Keys = Collections.emptyMap();
                sm4DefaultKeyId = null;
            }
            return;
        }
        Map<String, byte[]> next = new LinkedHashMap<>();
        String defaultId = null;
        for (String name : properties.stringPropertyNames()) {
            if ("sm4.defaultKeyId".equals(name)) {
                defaultId = properties.getProperty(name);
                if (StrUtil.isNotBlank(defaultId)) {
                    defaultId = defaultId.trim();
                } else {
                    defaultId = null;
                }
                continue;
            }
            if (name != null && name.startsWith("sm4.keys.")) {
                String keyId = name.substring("sm4.keys.".length());
                if (StrUtil.isBlank(keyId)) {
                    throw new IllegalArgumentException("非法的配置键: " + name);
                }
                String hex = properties.getProperty(name);
                next.put(keyId, parseSm4KeyMaterial(hex, keyId));
            }
        }
        synchronized (lock) {
            sm4Keys = Collections.unmodifiableMap(next);
            sm4DefaultKeyId = defaultId;
        }
    }

    private static byte[] parseSm4KeyMaterial(String hex, String keyId) {
        if (StrUtil.isBlank(hex)) {
            throw new IllegalArgumentException("SM4 密钥为空: sm4.keys." + keyId);
        }
        String compact = hex.trim().replace(" ", "");
        if (!HEX_KEY_PATTERN.matcher(compact).matches()) {
            throw new IllegalArgumentException(
                    "SM4 密钥须为 32 位十六进制（16 字节）: sm4.keys." + keyId);
        }
        return HexUtil.decodeHex(compact);
    }

    @Override
    public String encrypt(String rawPassword, String codecId) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword 不能为 null");
        }
        if (StrUtil.isBlank(codecId)) {
            throw new IllegalArgumentException("codecId 不能为空");
        }
        String id = codecId.trim();
        if ("bcrypt".equalsIgnoreCase(id)) {
            String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
            return "{bcrypt}" + hash;
        }
        if ("sm4".equalsIgnoreCase(id)) {
            String def = sm4DefaultKeyId;
            if (StrUtil.isBlank(def)) {
                throw new IllegalStateException(
                        "codecId 为 sm4 但未配置 sm4.defaultKeyId");
            }
            return encryptSm4(rawPassword, def);
        }
        if (id.regionMatches(true, 0, SM4_PREFIX, 0, SM4_PREFIX.length())) {
            String keyId = id.substring(SM4_PREFIX.length());
            if (StrUtil.isBlank(keyId)) {
                throw new IllegalArgumentException("sm4:keyId 中 keyId 不能为空");
            }
            return encryptSm4(rawPassword, keyId);
        }
        throw new IllegalArgumentException("不支持的 codecId: " + codecId);
    }

    @Override
    public boolean matches(String rawPassword, String prefixEncoded) {
        if (rawPassword == null || prefixEncoded == null) {
            return false;
        }
        ParsedPrefix parsed = parsePrefix(prefixEncoded);
        if (parsed == null) {
            return BCrypt.checkpw(rawPassword, prefixEncoded);
        }
        String inner = parsed.id;
        String payload = parsed.payload;
        if ("bcrypt".equalsIgnoreCase(inner)) {
            return BCrypt.checkpw(rawPassword, payload);
        }
        if (inner.regionMatches(true, 0, SM4_PREFIX, 0, SM4_PREFIX.length())) {
            String keyId = inner.substring(SM4_PREFIX.length());
            try {
                String plain = decryptSm4(keyId, payload);
                return constantTimeEquals(plain.getBytes(StandardCharsets.UTF_8),
                        rawPassword.getBytes(StandardCharsets.UTF_8));
            } catch (RuntimeException ignored) {
                return false;
            }
        }
        throw new IllegalArgumentException("不支持的前缀算法: {" + inner + "}");
    }

    @Override
    public String decrypt(String prefixEncoded) {
        if (StrUtil.isBlank(prefixEncoded)) {
            throw new IllegalArgumentException("prefixEncoded 不能为空");
        }
        ParsedPrefix parsed = parsePrefix(prefixEncoded);
        if (parsed == null) {
            throw new IllegalStateException("仅支持带 {sm4:keyId} 前缀的串解密，bcrypt 不可逆");
        }
        String inner = parsed.id;
        if ("bcrypt".equalsIgnoreCase(inner)) {
            throw new IllegalStateException("bcrypt 不可逆，不支持解密");
        }
        if (inner.regionMatches(true, 0, SM4_PREFIX, 0, SM4_PREFIX.length())) {
            String keyId = inner.substring(SM4_PREFIX.length());
            return decryptSm4(keyId, parsed.payload);
        }
        throw new IllegalArgumentException("不支持解密的编码类型: {" + inner + "}");
    }

    private String encryptSm4(String raw, String keyId) {
        byte[] key = requireSm4Key(keyId);
        SM4 sm4 = new SM4(Mode.CBC, Padding.PKCS5Padding, key);
        byte[] cipher = sm4.encrypt(raw.getBytes(StandardCharsets.UTF_8));
        String hex = HexUtil.encodeHexStr(cipher).toLowerCase(Locale.ROOT);
        return "{" + SM4_PREFIX + keyId + "}" + hex;
    }

    private String decryptSm4(String keyId, String hexPayload) {
        if (StrUtil.isBlank(hexPayload)) {
            throw new IllegalArgumentException("SM4 负载为空");
        }
        byte[] key = requireSm4Key(keyId);
        SM4 sm4 = new SM4(Mode.CBC, Padding.PKCS5Padding, key);
        byte[] cipherBytes = HexUtil.decodeHex(hexPayload.trim());
        byte[] plain = sm4.decrypt(cipherBytes);
        return new String(plain, StandardCharsets.UTF_8);
    }

    private byte[] requireSm4Key(String keyId) {
        Map<String, byte[]> map = sm4Keys;
        byte[] key = map.get(keyId);
        if (key == null) {
            throw new IllegalStateException("未注册的 SM4 keyId: " + keyId);
        }
        return key;
    }

    private static ParsedPrefix parsePrefix(String encoded) {
        if (!encoded.startsWith(PREFIX_BEGIN)) {
            return null;
        }
        int end = encoded.indexOf('}', 1);
        if (end <= 1) {
            return null;
        }
        String id = encoded.substring(1, end);
        String payload = encoded.substring(end + 1);
        return new ParsedPrefix(id, payload);
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(a, b);
    }

    private static final class ParsedPrefix {
        private final String id;
        private final String payload;

        ParsedPrefix(String id, String payload) {
            this.id = id;
            this.payload = payload;
        }
    }
}
