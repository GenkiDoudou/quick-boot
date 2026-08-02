package io.github.genkidoudou.common.crypto;

import cn.hutool.crypto.digest.BCrypt;

public class BCryptPasswordCodec extends AbstractValidatingPasswordCodec {
  @Override
  protected String encryptNonNullPassword(String rawPassword) {
    return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
  }

  @Override
  protected boolean matchesNonNull(String rawPassword, String encodedPassword) {
    return BCrypt.checkpw(rawPassword, encodedPassword);
  }

  @Override
  protected String decryptNonNullPassword(String encodedPassword) {
    throw new UnsupportedOperationException("不支持的方法");
  }
}
