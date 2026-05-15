package io.github.genkidoudou.common.firewall.client;

import java.util.List;

/**
 * 客户端服务接口
 * 定义统一的客户端查询接口，支持多种实现方式
 *
 * @author luyanan
 * @since 2026-03-04
 */
public interface ClientService {

    /**
     * 根据客户端ID获取客户端信息
     *
     * @param clientId 客户端ID
     * @return 客户端信息，不存在返回null
     */
    OauthClient getClientById(String clientId);

    /**
     * 验证客户端
     *
     * @param clientId     客户端ID
     * @param clientSecret 客户端密钥
     * @return 验证是否通过
     */
    boolean validateClient(String clientId, String clientSecret);

    /**
     * 获取所有启用的客户端
     *
     * @return 启用的客户端列表
     */
    List<OauthClient> getAllEnabledClients();


    /**
     * 检查前端传输的客户端id是否正确
     *
     * @param clientId 客户端id
     * @return
     * @since 2026/3/7
     */
    OauthClient parserClientId(String clientIdStr);


    String authClientIdEncrypt(OauthClient oauthClient);
}
