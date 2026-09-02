package io.github.genkidoudou.web.monitor;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.monitor.slowsql.SlowSqlType;
import io.github.genkidoudou.common.monitor.slowsql.SlowSqlTypeResolver;
import io.github.genkidoudou.monitor.internal.slowsql.dto.SysSlowSqlQueryBo;
import io.github.genkidoudou.monitor.internal.slowsql.dto.SysSlowSqlVo;
import io.github.genkidoudou.monitor.internal.slowsql.service.SysSlowSqlService;
import io.github.genkidoudou.web.support.QuickbootIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 慢 SQL：类型解析单元契约 + monitor 分页冒烟（方案 A2 SlowSqlCaptureIT 精简版）。
 */
@Transactional
class SlowSqlCaptureIT extends QuickbootIntegrationTestBase {

  @Autowired
  private SysSlowSqlService slowSqlService;

  @Test
  void typeResolverRecognizesSelect() {
    assertEquals(SlowSqlType.SELECT, SlowSqlTypeResolver.resolve("select * from sys_user"));
  }

  @Test
  void typeResolverRecognizesInsert() {
    assertEquals(SlowSqlType.INSERT, SlowSqlTypeResolver.resolve("INSERT INTO t(a) VALUES (1)"));
  }

  @Test
  void typeResolverRecognizesUpdate() {
    assertEquals(SlowSqlType.UPDATE, SlowSqlTypeResolver.resolve("UPDATE t SET a=1"));
  }

  @Test
  void typeResolverRecognizesDelete() {
    assertEquals(SlowSqlType.DELETE, SlowSqlTypeResolver.resolve("DELETE FROM t WHERE id=1"));
  }

  @Test
  void typeResolverBlankIsOther() {
    assertEquals(SlowSqlType.OTHER, SlowSqlTypeResolver.resolve("   "));
  }

  @Test
  void slowSqlPageSmoke() {
    SysSlowSqlQueryBo query = new SysSlowSqlQueryBo();
    query.setPageNum(1);
    query.setPageSize(10);
    PageInfo<SysSlowSqlVo> page = slowSqlService.page(query);
    assertNotNull(page);
    assertNotNull(page.getRecords());
  }
}
