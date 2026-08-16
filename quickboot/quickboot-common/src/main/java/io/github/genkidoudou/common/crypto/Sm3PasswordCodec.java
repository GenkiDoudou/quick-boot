package io.github.genkidoudou.common.crypto;


import cn.hutool.crypto.SmUtil;

/**
 * 国密 SM3 单向摘要密码编解码器：加密为 hex 摘要，不支持解密。
 */
public class Sm3PasswordCodec extends AbstractValidatingPasswordCodec {
  @Override
  protected String encryptNonNullPassword(String rawPassword) {
    return SmUtil.sm3().digestHex(rawPassword);
  }

  @Override
  protected boolean matchesNonNull(String rawPassword, String encodedPassword) {
    return this.encrypt(rawPassword)
      .equals(encodedPassword);
  }

  @Override
  protected String decryptNonNullPassword(String encodedPassword) {
    throw new UnsupportedOperationException("不允许的操作");
  }
}
