package io.github.genkidoudou.web.system;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.system.internal.service.ISysDictTypeService;
import io.github.genkidoudou.system.internal.vo.SysDictTypeVo;
import io.github.genkidoudou.web.support.QuickbootIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 字典类型 Tier-1 CRUD 冒烟：Vo-only Service 分页。
 */
@Transactional
class SysDictTypeCrudIT extends QuickbootIntegrationTestBase {

  @Autowired
  private ISysDictTypeService dictTypeService;

  @Test
  void pageReturnsVoList() {
    PageRequest<SysDictTypeVo> request = new PageRequest<>();
    request.setCurrent(1);
    request.setSize(10);
    PageInfo<SysDictTypeVo> page = dictTypeService.page(request);
    assertNotNull(page);
    assertNotNull(page.getRecords());
  }
}
