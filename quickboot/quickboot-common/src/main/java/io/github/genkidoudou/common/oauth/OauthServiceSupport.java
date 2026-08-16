package io.github.genkidoudou.common.oauth;

/**
 * OAuth 客户端数据访问 SPI：由 system 模块基于 {@code sys_oauth_client} 实现。
 */
public interface OauthServiceSupport {

  /**
   * 按客户端 id 查询配置。
   *
   * @param clientId 客户端 id
   * @return 客户端 VO；不存在时 {@code null}
   */
  OauthClientVo findByClientId(String clientId);
  

}
