package io.github.genkidoudou.common.oauth;

import io.github.genkidoudou.common.crypto.AbstractValidatingPasswordCodec;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RequiredArgsConstructor
public class ClientBasicPasswordCodes extends AbstractValidatingPasswordCodec {

  /**
   * XOR 盐
   *
   * @return
   * @since 2026/7/29
   */

  private final String key;


  @Override
  protected String encryptNonNullPassword(String rawPassword) {
    if (rawPassword == null) {
      return null;
    }
    String s = Base64.getUrlEncoder().withoutPadding().encodeToString(xor(rawPassword.getBytes(StandardCharsets.UTF_8)));
    return s;
  }

  @Override
  protected boolean matchesNonNull(String rawPassword, String encodedPassword) {
    return encrypt(rawPassword)
      .equals(encodedPassword);
  }

  @Override
  protected String decryptNonNullPassword(String encodedPassword) {
    if (encodedPassword == null || encodedPassword.isBlank()) {
      throw new IllegalArgumentException("empty obfuscated credential");
    }
    byte[] decoded = Base64.getUrlDecoder().decode(encodedPassword);
    return new String(xor(decoded), StandardCharsets.UTF_8);
  }

  private byte[] xor(byte[] data) {
    byte[] KEY = key.getBytes(StandardCharsets.UTF_8);
    byte[] out = new byte[data.length];
    for (int i = 0; i < data.length; i++) {
      out[i] = (byte) (data[i] ^ KEY[i % KEY.length]);
    }
    return out;
  }
}
