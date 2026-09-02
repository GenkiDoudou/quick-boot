package io.github.genkidoudou.web.quartz;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.quartz.internal.dto.SysJobLogQueryBo;
import io.github.genkidoudou.quartz.internal.dto.SysJobQueryBo;
import io.github.genkidoudou.quartz.internal.dto.SysJobLogVo;
import io.github.genkidoudou.quartz.internal.dto.SysJobVo;
import io.github.genkidoudou.quartz.internal.service.SysJobLogService;
import io.github.genkidoudou.quartz.internal.service.SysJobService;
import io.github.genkidoudou.web.support.QuickbootIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Quartz 任务 / 任务日志分页冒烟（POST page 契约背后的 Service）。
 */
@Transactional
class QuartzJobPageIT extends QuickbootIntegrationTestBase {

  @Autowired
  private SysJobService jobService;

  @Autowired
  private SysJobLogService jobLogService;

  @Test
  void jobPageSmoke() {
    SysJobQueryBo query = new SysJobQueryBo();
    query.setPageNum(1);
    query.setPageSize(10);
    PageInfo<SysJobVo> page = jobService.page(query);
    assertNotNull(page);
    assertNotNull(page.getRecords());
  }

  @Test
  void jobLogPageSmoke() {
    SysJobLogQueryBo query = new SysJobLogQueryBo();
    query.setPageNum(1);
    query.setPageSize(10);
    PageInfo<SysJobLogVo> page = jobLogService.page(query);
    assertNotNull(page);
    assertNotNull(page.getRecords());
  }
}
