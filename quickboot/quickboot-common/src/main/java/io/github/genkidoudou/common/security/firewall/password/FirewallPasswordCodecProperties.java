package io.github.genkidoudou.common.security.firewall.password;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 密码编解码防火墙配置，绑定前缀 {@code qc.security.firewall.password.codec}。
 * <p>
 * 与 {@link PasswordCodec#setProperties(Properties)} 对齐的扁平键：
 * {@code sm4.defaultKeyId}、{@code sm4.keys.<keyId>}（值为 32 位十六进制，表示 16 字节 SM4 密钥）。
 * </p>
 * <p>YAML 示例：</p>
 * <pre>
 * qc:
 *   security:
 *     firewall:
 *       password:
 *         codec:
 *           sm4:
 *             default-key-id: clientA
 *             keys:
 *               clientA: "0123456789abcdef0123456789abcdef"
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "qc.security.firewall.password.codec")
public class FirewallPasswordCodecProperties {

    private Sm4 sm4 = new Sm4();

    @Data
    public static class Sm4 {

        /**
         * 与 {@code encrypt(raw, "sm4")} 对应的默认 keyId。
         */
        private String defaultKeyId;

        /**
         * keyId → 32 位十六进制 SM4 密钥材料（16 字节）。
         */
        private Map<String, String> keys = new LinkedHashMap<>();
    }

    /**
     * 转为 {@link DefaultPasswordCodec#setProperties(Properties)} 可用的扁平属性。
     *
     * @return 非 {@code null} 的 {@link Properties}
     */
    public Properties toCodecProperties() {
        Properties p = new Properties();
        Sm4 s = sm4 != null ? sm4 : new Sm4();
        if (s.getDefaultKeyId() != null && !s.getDefaultKeyId().isBlank()) {
            p.setProperty("sm4.defaultKeyId", s.getDefaultKeyId().trim());
        }
        if (s.getKeys() != null) {
            for (Map.Entry<String, String> e : s.getKeys().entrySet()) {
                if (e.getKey() == null || e.getKey().isBlank()) {
                    continue;
                }
                String v = e.getValue();
                if (v != null) {
                    p.setProperty("sm4.keys." + e.getKey().trim(), v);
                }
            }
        }
        return p;
    }
}
