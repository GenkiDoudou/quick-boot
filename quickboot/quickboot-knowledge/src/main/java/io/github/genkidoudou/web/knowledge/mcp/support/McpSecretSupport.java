package io.github.genkidoudou.web.knowledge.mcp.support;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.security.firewall.password.PasswordCodec;
import io.github.genkidoudou.web.knowledge.constants.McpEnvValueType;

/**
 * MCP 环境变量与请求头密钥的 SM4 加解密，模式对齐 {@link io.github.genkidoudou.web.system.oauthclient.support.Oauth2SecretSupport}。
 */
public final class McpSecretSupport {

    /** 列表与默认详情中的脱敏占位符。 */
    public static final String MASK = "******";

    private McpSecretSupport() {
    }

    /**
     * 按值类型解析运行时明文。
     *
     * @param codec     项目密码编解码器
     * @param valueType PLAIN / SECRET / ENV_REF
     * @param stored    库中存储值
     * @return 运行时明文；ENV_REF 缺失时返回 null
     */
    public static String resolvePlainValue(PasswordCodec codec, String valueType, String stored) {
        if (StrUtil.isBlank(valueType)) {
            valueType = McpEnvValueType.PLAIN;
        }
        return switch (valueType) {
            case McpEnvValueType.SECRET -> resolvePlainSecret(codec, stored);
            case McpEnvValueType.ENV_REF -> resolveEnvRef(stored);
            default -> StrUtil.nullToEmpty(stored);
        };
    }

    /**
     * 解析 SM4 密文或明文密钥。
     *
     * @param codec  编解码器
     * @param stored 库中密文或明文
     * @return 明文
     */
    public static String resolvePlainSecret(PasswordCodec codec, String stored) {
        if (StrUtil.isBlank(stored)) {
            return "";
        }
        String s = stored.trim();
        if (s.startsWith("{sm4")) {
            return codec.decrypt(s);
        }
        return s;
    }

    /**
     * 入库加密：优先 SM4。
     *
     * @param codec 编解码器
     * @param plain 明文
     * @return SM4 密文
     */
    public static String encodeForStorage(PasswordCodec codec, String plain) {
        if (StrUtil.isBlank(plain)) {
            return "";
        }
        return codec.encrypt(plain, "sm4");
    }

    /**
     * 列表/详情脱敏展示。
     *
     * @param valueType 值类型
     * @param stored    库中值
     * @param reveal    是否展示明文
     * @return 展示值
     */
    public static String maskForDisplay(String valueType, String stored, boolean reveal) {
        if (reveal) {
            return stored;
        }
        if (McpEnvValueType.SECRET.equals(valueType)) {
            return MASK;
        }
        return stored;
    }

    /**
     * 判断请求体中的密钥字段是否表示「不修改原值」。
     *
     * @param valueType 值类型
     * @param submitted 提交值
     * @return true 表示保留库中原值
     */
    public static boolean isKeepExistingSecret(String valueType, String submitted) {
        return McpEnvValueType.SECRET.equals(valueType) && StrUtil.isBlank(submitted);
    }

    private static String resolveEnvRef(String envName) {
        if (StrUtil.isBlank(envName)) {
            return null;
        }
        String name = envName.trim();
        String value = System.getenv(name);
        return value;
    }
}
