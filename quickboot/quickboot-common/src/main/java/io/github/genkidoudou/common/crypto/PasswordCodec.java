package io.github.genkidoudou.common.crypto;

import java.util.Properties;

/**
 * 加密算法
 *
 * @author luyanan
 * @since 2026/7/26
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
  default void setProperties(Properties properties) {

  }

  /**
   * 按算法标识加密明文。
   *
   * @param rawPassword 明文
   * @return 带 {@code {id}} 前缀的编码串（具体格式由实现决定）
   */
  String encrypt(String rawPassword);


  /**
   * 校验明文是否与已存储串一致。若 {@code prefixEncoded} 无 {@code {...}} 前缀，则按<strong>默认 bcrypt</strong> 校验整串（兼容历史存根）。
   *
   * @param rawPassword     明文
   * @param encodedPassword 带前缀密文或不带前缀的 bcrypt 哈希
   * @return 是否匹配
   */
  boolean matches(CharSequence rawPassword, String encodedPassword);

  /**
   * 解密
   *
   * @param prefixEncoded 带 SM4 前缀与十六进制负载的串
   * @return 明文
   */
  String decrypt(String prefixEncoded);
}
