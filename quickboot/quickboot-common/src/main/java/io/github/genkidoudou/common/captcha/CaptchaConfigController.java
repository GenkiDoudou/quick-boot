package io.github.genkidoudou.common.captcha;

import io.github.genkidoudou.common.oauth.OauthClientVo;
import io.github.genkidoudou.common.oauth.utils.OauthClientUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 登录页验证码开关（不受 {@code qc.captcha.enabled} 条件装配影响，关闭时也返回 false）。
 */
@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
public class CaptchaConfigController {

  private final CaptchaProperties captchaProperties;

  /**
   * @return {@code captchaEnabled} / {@code type}
   */
  @GetMapping("/config")
  public Map<String, Object> config() {
    Map<String, Object> data = new LinkedHashMap<>(4);
    data.put("captchaEnabled", OauthClientUtils.isEnable());
    data.put("type", captchaProperties.getType());
    return data;
  }
}
