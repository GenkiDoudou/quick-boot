package io.github.genkidoudou.web.system.oauthclient.service;

import io.github.genkidoudou.web.system.oauthclient.domain.SysOauthClient;
import io.github.genkidoudou.web.system.oauthclient.dto.SysOauthClientBo;
import io.github.genkidoudou.web.system.oauthclient.dto.SysOauthClientCredentialsVo;
import io.github.genkidoudou.web.system.oauthclient.dto.SysOauthClientVo;

import java.util.List;

/**
 * OAuth 客户端管理服务。
 */
public interface SysOauthClientService {

    List<SysOauthClient> list(String clientName);

    SysOauthClient getById(String clientId);

    /**
     * 管理端详情（不含 client_secret）。
     *
     * @param clientId 客户端 ID
     * @return 详情 VO
     */
    SysOauthClientVo getDetailVo(String clientId);

    /**
     * 校验当前用户密码后返回明文 client_id / client_secret。
     *
     * @param clientId        客户端 ID
     * @param currentPassword 当前登录用户密码
     */
    SysOauthClientCredentialsVo revealCredentials(String clientId, String currentPassword);

    void add(SysOauthClientBo req);

    void update(SysOauthClientBo req);

    void remove(List<String> clientIds);
}
