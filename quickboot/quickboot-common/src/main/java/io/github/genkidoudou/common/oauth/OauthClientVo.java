package io.github.genkidoudou.common.oauth;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.List;

/**
 * 客户端
 *
 * @author luyanan
 * @since 2026/7/27
 */
@Data
public class OauthClientVo {

  public static final String ATTR_KEY = "oauthClient";


  private String clientId;

  private String clientSecret;

  /**
   * 允许访问的接口 path（Ant 风格，如 /system/**），每行一条
   */
  private List<String> apiPathPatterns;


  /**
   * token有效时间
   *
   * @since 2026/8/1
   */
  private Long tokenTimeout;


  /**
   * 是否校验验证码：{@code 0}=否，{@code 1}=是
   *
   * @since 2026/8/1
   */
  private String checkCaptcha;


}
