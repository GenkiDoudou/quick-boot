package io.github.genkidoudou.system.internal.support;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.exception.ErrorException;
import io.github.genkidoudou.common.oauth.OauthClientVo;
import io.github.genkidoudou.common.oauth.OauthServiceSupport;
import io.github.genkidoudou.core.entity.enums.CommonEnums;
import io.github.genkidoudou.system.internal.entity.SysOauthClient;
import io.github.genkidoudou.system.internal.service.ISysOauthClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
@Component
public class OauthServiceSupportImpl implements OauthServiceSupport {

  private final ISysOauthClientService iSysOauthClientService;

  @Override
  public OauthClientVo findByClientId(String clientId) {
    SysOauthClient sysOauthClient = iSysOauthClientService.findByClientId(clientId);
    if (null == sysOauthClient) {
      throw new ErrorException(600);

    }
    String status = sysOauthClient.getStatus();
    if (!status.equals(CommonEnums.STATUS_ENABLE.getValue())) {
      throw new ErrorException(601);
    }
    OauthClientVo oauthClientVo = new OauthClientVo();
    oauthClientVo.setClientId(sysOauthClient.getClientId());
    oauthClientVo.setClientSecret(sysOauthClient.getClientSecret());
    String patterns = sysOauthClient.getApiPathPatterns();
    if (StrUtil.isNotBlank(patterns)) {
      oauthClientVo.setApiPathPatterns(Arrays.stream(patterns.split(","))
        .map(String::trim)
        .filter(StrUtil::isNotBlank)
        .toList());
    }
    oauthClientVo.setTokenTimeout(sysOauthClient.getTokenTimeout());
    oauthClientVo.setCheckCaptcha(sysOauthClient.getCheckCaptcha());
    return oauthClientVo;
  }

}
