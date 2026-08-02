package io.github.genkidoudou.common.crypto;

import cn.hutool.extra.spring.SpringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 密码编码器工厂
 *
 * @author luyanan
 * @since 2026/7/29
 */

public class PasswordCodecFactories {

  public static DelegatingPasswordCodec createPasswordCodec() {
    String encodingId = "bcrypt";
    Map<String, PasswordCodec> encoders = new HashMap<>();
    encoders.put(encodingId, new BCryptPasswordCodec());
    encoders.put("sm3", new Sm3PasswordCodec());

    return new DelegatingPasswordCodec(encodingId, encoders);
  }


  /**
   * 注册
   *
   * @param idForEncode
   * @param passwordCodec
   * @return
   * @since 2026/7/29
   */
  public static void register(String idForEncode, PasswordCodec passwordCodec) {
    resolveDelegating().register(idForEncode, passwordCodec);
  }


  public static PasswordCodec get(String idForEncode) {
    return resolveDelegating().get(idForEncode);
  }

  private static DelegatingPasswordCodec resolveDelegating() {
    PasswordCodec codec = SpringUtil.getBean(PasswordCodec.class);
    if (codec instanceof DelegatingPasswordCodec delegating) {
      return delegating;
    }
    throw new IllegalStateException(
      "PasswordCodec bean must be DelegatingPasswordCodec, actual: "
        + (codec == null ? "null" : codec.getClass().getName()));
  }

}
