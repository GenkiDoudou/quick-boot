package io.github.genkidoudou.common.oauth.config;

import io.github.genkidoudou.common.common.Constants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = Constants.PROPERTIES_PREFIX + ".oauth")
public class OauthClientProperties {

  /**
   * 是否开启
   *
   * @since 2026/7/29
   */
  private boolean enable = false;


  private String key;

  /**
   * 忽略的url
   *
   * @author luyanan
   * @since 2026/7/29
   */
  private List<String> ignoreUrl;


}
