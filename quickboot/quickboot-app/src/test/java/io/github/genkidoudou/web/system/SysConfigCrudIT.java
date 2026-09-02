package io.github.genkidoudou.web.system;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.system.internal.service.ISysConfigService;
import io.github.genkidoudou.system.internal.vo.SysConfigVo;
import io.github.genkidoudou.web.support.QuickbootIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 参数配置 Tier-1 CRUD 冒烟：验证 {@link CrudServiceImpl} 试点域分页与新增（Vo-only Service API）。
 */
@Transactional
class SysConfigCrudIT extends QuickbootIntegrationTestBase {

  @Autowired
  private ISysConfigService configService;

  @Test
  void pageReturnsVoList() {
    PageRequest<SysConfigVo> request = new PageRequest<>();
    request.setCurrent(1);
    request.setSize(10);
    PageInfo<SysConfigVo> page = configService.page(request);
    assertNotNull(page);
    assertNotNull(page.getRecords());
  }

  @Test
  void addConfigViaVo() {
    SysConfigVo vo = new SysConfigVo();
    vo.setConfigName("集成测试参数");
    vo.setConfigKey("it.crud.config.key");
    vo.setConfigValue("1");
    vo.setConfigType("0");
    Long id = configService.add(vo);
    assertNotNull(id);
    SysConfigVo detail = configService.getDetail(id);
    assertNotNull(detail);
    assertTrue("it.crud.config.key".equals(detail.getConfigKey()));
  }
}
