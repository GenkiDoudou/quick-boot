package io.github.genkidoudou.common.crypto;

import cn.hutool.core.lang.Opt;

import java.util.Properties;

public abstract class AbstractValidatingPasswordCodec implements PasswordCodec {


  /**
   * 配置
   *
   * @since 2026/7/29
   */

  protected Properties properties;


  @Override
  public void setProperties(Properties properties) {
    this.properties = Opt.ofNullable(properties).orElse(new Properties());
  }

  protected void setProperties(String key, Object value) {
    if (this.properties == null) {
      this.properties = new Properties();
    }
    this.properties.put(key, value);
  }


  /**
   * 根据key 获取配置
   * @since 2026/7/29
 * @param key
   * @return
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
   * 文本加密
   *
   * @param rawPassword 文本
   * @return
   * @since 2026/7/27
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

  protected abstract boolean matchesNonNull(String rawPassword, String encodedPassword);

  @Override
  public String decrypt(String encodedPassword) {
    if (null == encodedPassword) {
      return null;
    }
    return decryptNonNullPassword(encodedPassword);
  }


  /**
   * 文本解密
   *
   * @param encodedPassword 密文
   * @return
   * @since 2026/7/27
   */
  protected abstract String decryptNonNullPassword(String encodedPassword);
}
