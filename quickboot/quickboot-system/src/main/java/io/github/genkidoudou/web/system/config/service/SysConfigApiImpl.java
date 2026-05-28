package io.github.genkidoudou.web.system.config.service;

import io.github.genkidoudou.core.service.SysConfigApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
@Slf4j
public class SysConfigApiImpl implements SysConfigApi {


  private final SysConfigService sysConfigService;

  @Override
  public String getConfigValueByKey(String configKey) {
    return sysConfigService.getConfigValueByKey(configKey);
  }
}
