package io.github.genkidoudou.common.crypto;

import cn.hutool.core.lang.Opt;

import java.util.Properties;

/**
 * {@link PasswordCodec} 抽象基类：统一 null/空串边界，子类只需实现非空分支。
 */
public abstract class AbstractValidatingPasswordCodec implements PasswordCodec {

  /** 算法可选配置（由 {@link #setProperties(Properties)} 注入）。 */
  protected Properties properties;


  @Override
  public void setProperties(Properties properties) {
    this.properties = Opt.ofNullable(properties).orElse(new Properties());
  }

  /**
   * 写入单条配置项（懒初始化 {@link #properties}）。
   *
   * @param key   配置键
   * @param value 配置值
   */
  protected void setProperties(String key, Object value) {
    if (this.properties == null) {
      this.properties = new Properties();
    }
    this.properties.put(key, value);
  }


  /**
   * 读取字符串配置项。
   *
   * @param key 配置键
   * @return 配置值；未注入 properties 或键不存在时返回 {@code null}
   */
  protected String getConfig(String key) {
    if (null == this.properties) {
      return null;
    }
    return this.properties.getProperty(key);
  }

  @Override
  public String encrypt(String rawPassword) {
    if (null == rawPassword) {
      return null;
    }
    return encryptNonNullPassword(rawPassword);
  }

  /**
   * 加密非空明文（由 {@link #encrypt(String)} 在 null 校验后调用）。
   *
   * @param rawPassword 非空明文
   * @return 密文或带算法前缀的编码串
   */
  protected abstract String encryptNonNullPassword(String rawPassword);

  @Override
  public boolean matches(CharSequence rawPassword, String encodedPassword) {
    if (rawPassword == null || rawPassword.isEmpty() || encodedPassword == null
      || encodedPassword.isEmpty()) {
      return false;
    }
    return matchesNonNull(rawPassword.toString(), encodedPassword);
  }

  /**
   * 校验非空明文与密文是否匹配。
   *
   * @param rawPassword     非空明文
   * @param encodedPassword 非空密文（不含 delegating 外层前缀时由子类约定）
   * @return 是否匹配
   */
  protected abstract boolean matchesNonNull(String rawPassword, String encodedPassword);

  @Override
  public String decrypt(String encodedPassword) {
    if (null == encodedPassword) {
      return null;
    }
    return decryptNonNullPassword(encodedPassword);
  }


  /**
   * 解密非空密文；单向哈希实现可抛 {@link UnsupportedOperationException}。
   *
   * @param encodedPassword 非空密文
   * @return 明文
   */
  protected abstract String decryptNonNullPassword(String encodedPassword);
}
