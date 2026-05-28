package io.github.genkidoudou.web.system.oauthprovider.service;

import io.github.genkidoudou.web.system.oauthprovider.domain.SysOauthProvider;
import io.github.genkidoudou.web.system.oauthprovider.dto.SysOauthProviderBo;

import java.util.List;

/**
 * 外部 IdP 配置服务。
 */
public interface SysOauthProviderService {

    List<SysOauthProvider> list(String providerName);

    List<SysOauthProvider> listEnabledForLogin();

    SysOauthProvider getByCode(String providerCode);

    void add(SysOauthProviderBo req);

    void update(SysOauthProviderBo req);

    void remove(List<String> providerCodes);
}
