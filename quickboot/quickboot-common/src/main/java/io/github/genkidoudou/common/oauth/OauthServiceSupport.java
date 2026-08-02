package io.github.genkidoudou.common.oauth;

public interface OauthServiceSupport {


  /**
   * 根据客户端id查询客户端
   *
   * @param clientId 客户端id
   * @return
   * @since 2026/7/27
   */
  OauthClientVo findByClientId(String clientId);
  

}
