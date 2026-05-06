package io.github.genkidoudou.common.security.firewall.password;

import java.util.Properties;

/**
 * 防火墙场景下的密码与认证串编解码：支持 {@code {bcrypt}...} 与 {@code {sm4:keyId}...} 前缀格式。
 * <p>
 * 典型在 Spring 容器启动阶段通过 {@link #setProperties(Properties)} 注入 SM4 多密钥；此后实例视为配置只读（实现可多线程并发调用 {@link #encrypt}/{@link #matches}/{@link #decrypt}）。
 * </p>
 * <p>
 * 本契约<strong>不</strong>使用 Spring Security 的 {@code PasswordEncoder}，算法由 Hutool（BCrypt + 国密 SM4）完成。
 * </p>
 *
 * @author quickboot-common
 */
public interface PasswordCodec {

    /**
     * 在首次编解码前注入配置。SM4 密钥形如 {@code sm4.keys.<keyId>=<32位十六进制>}（表示 16 字节密钥），可选 {@code sm4.defaultKeyId}。
     * <p>
     * 应在应用启动装配阶段调用；多次调用时以后者覆盖内部缓存（便于测试），生产环境建议仅调用一次。
     * </p>
     *
     * @param properties 扁平属性集，键与 {@code design} / 自动配置前缀一致
     */
    void setProperties(Properties properties);

    /**
     * 按算法标识加密明文。
     * <p>
     * {@code codecId} 取值为 {@code bcrypt}、{@code sm4}（使用默认 keyId）或 {@code sm4:<keyId>}。
     * </p>
     *
     * @param rawPassword 明文
     * @param codecId     算法与 SM4 key 选择
     * @return 带 {@code {id}} 前缀的编码串
     */
    String encrypt(String rawPassword, String codecId);

    /**
     * 校验明文是否与已存储串一致。若 {@code prefixEncoded} 无 {@code {...}} 前缀，则按<strong>默认 bcrypt</strong> 校验整串（兼容历史存根）。
     *
     * @param rawPassword     明文
     * @param prefixEncoded   带前缀密文或不带前缀的 bcrypt 哈希
     * @return 是否匹配
     */
    boolean matches(String rawPassword, String prefixEncoded);

    /**
     * 仅对 {@code {sm4:keyId}} 前缀串解密得到 UTF-8 明文；{@code bcrypt} 不可逆，调用将失败。
     *
     * @param prefixEncoded 带 SM4 前缀与十六进制负载的串
     * @return 明文
     */
    String decrypt(String prefixEncoded);
}
