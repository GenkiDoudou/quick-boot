package io.github.genkidoudou.common.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DelegatingPasswordCodecTest {

  @Test
  public void test() {
    String plainText = "1111111";
    PasswordCodec passwordCodec = PasswordCodecFactories.createPasswordCodec();
    String decrypt = passwordCodec.encrypt(plainText);
    boolean matches = passwordCodec.matches(plainText, decrypt);
    assertTrue(matches);
  }
}
