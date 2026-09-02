package io.github.genkidoudou.system.internal.support;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.exception.ErrorException;
import io.github.genkidoudou.common.oauth.OauthClientVo;
import io.github.genkidoudou.common.oauth.OauthServiceSupport;
import io.github.genkidoudou.core.entity.enums.CommonEnums;
import io.github.genkidoudou.system.internal.service.ISysOauthClientService;
import io.github.genkidoudou.system.internal.vo.SysOauthClientVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * OAuth 客户端解析：校验启用状态并组装 {@link OauthClientVo} 供登录过滤器使用。
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OauthServiceSupportImpl implements OauthServiceSupport {

  private final ISysOauthClientService iSysOauthClientService;

  /**
   * 按 clientId 查询并校验客户端状态，解析 API 路径白名单。
   *
   * @param clientId 客户端业务标识
   * @return 登录鉴权用 Vo
   */
  @Override
  public OauthClientVo findByClientId(String clientId) {
    SysOauthClientVo row = iSysOauthClientService.findByClientId(clientId);
    if (row == null) {
      throw new ErrorException(600);
    }
    String status = row.getStatus();
    if (status == null || !status.equals(CommonEnums.STATUS_ENABLE.getValue())) {
      throw new ErrorException(601);
    }
    OauthClientVo oauthClientVo = new OauthClientVo();
    oauthClientVo.setClientId(row.getClientId());
    oauthClientVo.setClientSecret(row.getClientSecret());
    String patterns = row.getApiPathPatterns();
    if (StrUtil.isNotBlank(patterns)) {
      oauthClientVo.setApiPathPatterns(Arrays.stream(patterns.split(","))
        .map(String::trim)
        .filter(StrUtil::isNotBlank)
        .toList());
    }
    oauthClientVo.setTokenTimeout(row.getTokenTimeout());
    oauthClientVo.setCheckCaptcha(row.getCheckCaptcha());
    return oauthClientVo;
  }
}
