package io.github.genkidoudou.common.crypto;

import cn.hutool.crypto.digest.BCrypt;

public class BCryptPasswordCodec  extends  AbstractValidatingPasswordCodec{
  @Override
  protected String encryptNonNullPassword(String rawPassword) {
    BCrypt.
    return "";
  }

  @Override
  protected boolean matchesNonNull(String rawPassword, String encodedPassword) {
    return false;
  }

  @Override
  protected String decryptNonNullPassword(String encodedPassword) {
    return "";
  }
}
