package io.github.genkidoudou.common.oauth.utils;

import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import io.github.genkidoudou.common.captcha.CaptchaProperties;
import io.github.genkidoudou.common.crypto.PasswordCodec;
import io.github.genkidoudou.common.crypto.PasswordCodecFactories;
import io.github.genkidoudou.common.exception.ErrorException;
import io.github.genkidoudou.common.oauth.ClientBasicPasswordCodes;
import io.github.genkidoudou.common.oauth.OauthClientVo;
import io.github.genkidoudou.common.security.utils.LoginUserUtils;
import io.github.genkidoudou.common.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OauthClientUtils {

  /**
   * 获取客户端信息
   *
   * @return 当前请求校验通过的客户端
   * @since 2026/7/29
   */
  public OauthClientVo getClient() {
    HttpServletRequest httpServletRequest = ServletUtils.currentRequest();
    return Opt.ofNullable(httpServletRequest)
      .map(a -> (OauthClientVo) a.getAttribute(OauthClientVo.ATTR_KEY))
      .orElseThrow(() -> new ErrorException(600));
  }

  /**
   * 生成客户端 Authorization 头：{@code Basic} + 混淆后的 {@code clientId:clientSecret}。
   * <p>
   * 与 {@code ClientBasicAuthenticationFilter} / {@code clientBasic} 编解码器一致。
   *
   * @param clientId     客户端 id
   * @param clientSecret 客户端密钥（明文）
   * @return 完整 Authorization 头值，如 {@code Basic xxxxx}
   * @since 2026/7/30
   */
  public String getHeader(String clientId, String clientSecret) {
    if (StrUtil.hasBlank(clientId, clientSecret)) {
      throw new IllegalArgumentException("clientId/clientSecret must not be blank");
    }
    PasswordCodec codec = PasswordCodecFactories.get("clientBasic");
    String obfuscated = codec.encrypt(clientId + ":" + clientSecret);
    return LoginUserUtils.BASIC + obfuscated;
  }

  public static void main(String[] args) {
    PasswordCodec passwordCodec = new ClientBasicPasswordCodes("QuickBootOAuth1");

    String clientId = "quick-ui:quick-ui-secret";
    String obfuscated = passwordCodec.encrypt(clientId);
//    String header = OauthClientUtils.getHeader("quick-ui", "quick-ui-secret");
    System.out.println(LoginUserUtils.BASIC + obfuscated);
    String decrypt = passwordCodec.decrypt(obfuscated);
    System.out.println(decrypt);
  }


  public boolean isEnable() {
    CaptchaProperties captchaProperties = SpringUtil.getBean(CaptchaProperties.class);
    if (null == captchaProperties) {
      return false;
    }
    boolean captchaEnabled = captchaProperties.getEnabled();
    if (captchaEnabled) {
      OauthClientVo client = OauthClientUtils.getClient();
      captchaEnabled = client.getCheckCaptcha().equals("1");
    }
    return captchaEnabled;
  }
}
