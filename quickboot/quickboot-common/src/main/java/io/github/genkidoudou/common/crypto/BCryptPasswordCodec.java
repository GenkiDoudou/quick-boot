package io.github.genkidoudou.common.crypto;

import cn.hutool.crypto.digest.BCrypt;

/**
 * BCrypt 密码编解码器：带盐哈希，适用于用户口令存储。
 */
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
