package io.github.genkidoudou.common.crypto;


import cn.hutool.crypto.SmUtil;

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
