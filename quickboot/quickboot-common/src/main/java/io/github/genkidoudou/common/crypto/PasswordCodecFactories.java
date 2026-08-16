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

  /**
   * 创建默认委托编解码器：加密默认 {@code bcrypt}，并注册 {@code sm3}。
   *
   * @return 未注入 Spring 时可独立使用的 {@link DelegatingPasswordCodec}
   */
  public static DelegatingPasswordCodec createPasswordCodec() {
    String encodingId = "bcrypt";
    Map<String, PasswordCodec> encoders = new HashMap<>();
    encoders.put(encodingId, new BCryptPasswordCodec());
    encoders.put("sm3", new Sm3PasswordCodec());

    return new DelegatingPasswordCodec(encodingId, encoders);
  }


  /**
   * 向 Spring 容器中的 {@link DelegatingPasswordCodec} 动态注册算法实现。
   *
   * @param idForEncode   算法 id（写入 {@code {id}} 前缀）
   * @param passwordCodec 实现
   */
  public static void register(String idForEncode, PasswordCodec passwordCodec) {
    resolveDelegating().register(idForEncode, passwordCodec);
  }


  /**
   * 按算法 id 获取已注册的 {@link PasswordCodec}。
   *
   * @param idForEncode 算法 id
   * @return 对应实现；不存在时返回 {@code null}
   */
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
