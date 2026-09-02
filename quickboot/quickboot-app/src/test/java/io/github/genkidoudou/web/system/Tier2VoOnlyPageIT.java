package io.github.genkidoudou.web.system;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.system.internal.service.ISysOauthClientService;
import io.github.genkidoudou.system.internal.service.ISysUserService;
import io.github.genkidoudou.system.internal.vo.SysOauthClientVo;
import io.github.genkidoudou.system.internal.vo.SysUserVo;
import io.github.genkidoudou.web.support.QuickbootIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tier-2 用户 / OAuth 客户端分页冒烟：公开 Service 仅返回 Vo。
 */
@Transactional
class Tier2VoOnlyPageIT extends QuickbootIntegrationTestBase {

  @Autowired
  private ISysUserService userService;

  @Autowired
  private ISysOauthClientService oauthClientService;

  @Test
  void userPageReturnsVo() {
    PageRequest<SysUserVo> request = new PageRequest<>();
    request.setCurrent(1);
    request.setSize(5);
    PageInfo<SysUserVo> page = userService.page(request);
    assertNotNull(page);
    assertNotNull(page.getRecords());
  }

  @Test
  void oauthClientPageMasksSecret() {
    PageRequest<SysOauthClientVo> request = new PageRequest<>();
    request.setCurrent(1);
    request.setSize(5);
    PageInfo<SysOauthClientVo> page = oauthClientService.page(request);
    assertNotNull(page);
    assertNotNull(page.getRecords());
    page.getRecords().forEach(row -> assertTrue(row.getClientSecret() == null));
  }
}
