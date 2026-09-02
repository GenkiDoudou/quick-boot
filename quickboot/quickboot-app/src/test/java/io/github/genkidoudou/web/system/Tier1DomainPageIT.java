package io.github.genkidoudou.web.system;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.system.internal.service.ISysDeployRecordService;
import io.github.genkidoudou.system.internal.service.ISysDictDataService;
import io.github.genkidoudou.system.internal.service.ISysFileClassifyService;
import io.github.genkidoudou.system.internal.service.ISysLogininforService;
import io.github.genkidoudou.system.internal.service.ISysOperLogService;
import io.github.genkidoudou.system.internal.vo.SysDeployRecordVo;
import io.github.genkidoudou.system.internal.vo.SysDictDataVo;
import io.github.genkidoudou.system.internal.vo.SysFileClassifyVo;
import io.github.genkidoudou.system.internal.vo.SysLogininforVo;
import io.github.genkidoudou.system.internal.vo.SysOperLogVo;
import io.github.genkidoudou.web.support.QuickbootIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tier-1 其余域分页冒烟：DictData / FileClassify / DeployRecord / OperLog / Logininfor。
 */
@Transactional
class Tier1DomainPageIT extends QuickbootIntegrationTestBase {

  @Autowired
  private ISysDictDataService dictDataService;

  @Autowired
  private ISysFileClassifyService fileClassifyService;

  @Autowired
  private ISysDeployRecordService deployRecordService;

  @Autowired
  private ISysOperLogService operLogService;

  @Autowired
  private ISysLogininforService logininforService;

  @Test
  void dictDataPage() {
    assertPage(dictDataService.page(req()));
  }

  @Test
  void fileClassifyPage() {
    assertPage(fileClassifyService.page(req()));
  }

  @Test
  void deployRecordPage() {
    assertPage(deployRecordService.page(req()));
  }

  @Test
  void operLogPage() {
    assertPage(operLogService.page(req()));
  }

  @Test
  void logininforPage() {
    assertPage(logininforService.page(req()));
  }

  private static <T> PageRequest<T> req() {
    PageRequest<T> r = new PageRequest<>();
    r.setCurrent(1);
    r.setSize(5);
    return r;
  }

  private static void assertPage(PageInfo<?> page) {
    assertNotNull(page);
    assertNotNull(page.getRecords());
  }
}
