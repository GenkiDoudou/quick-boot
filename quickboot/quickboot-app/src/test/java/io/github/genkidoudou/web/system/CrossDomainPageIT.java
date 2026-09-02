package io.github.genkidoudou.web.system;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.monitor.ExceptionReporter;
import io.github.genkidoudou.system.internal.online.dto.SysUserOnlineQueryBo;
import io.github.genkidoudou.system.internal.online.service.SysUserOnlineService;
import io.github.genkidoudou.system.internal.service.ISysDeptService;
import io.github.genkidoudou.system.internal.service.ISysFileClassifyService;
import io.github.genkidoudou.system.internal.service.ISysFileService;
import io.github.genkidoudou.system.internal.service.ISysOauthClientService;
import io.github.genkidoudou.system.internal.service.ISysRoleService;
import io.github.genkidoudou.system.internal.vo.SysDeptVo;
import io.github.genkidoudou.system.internal.vo.SysFileClassifyVo;
import io.github.genkidoudou.system.internal.vo.SysFileVo;
import io.github.genkidoudou.system.internal.vo.SysOauthClientVo;
import io.github.genkidoudou.system.internal.vo.SysRoleVo;
import io.github.genkidoudou.tool.internal.gen.dto.GenTableQueryBo;
import io.github.genkidoudou.tool.internal.gen.dto.GenTableVo;
import io.github.genkidoudou.tool.internal.gen.service.GenTableService;
import io.github.genkidoudou.web.support.QuickbootIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 横切 SPI + Tier-2 / Online / Gen / Dept 分页或列表冒烟，拉升集成测试覆盖。
 */
@Transactional
class CrossDomainPageIT extends QuickbootIntegrationTestBase {

  @Autowired
  private ISysRoleService roleService;

  @Autowired
  private ISysFileService fileService;

  @Autowired
  private ISysFileClassifyService fileClassifyService;

  @Autowired
  private ISysOauthClientService oauthClientService;

  @Autowired
  private ISysDeptService deptService;

  @Autowired
  private SysUserOnlineService onlineService;

  @Autowired
  private GenTableService genTableService;

  @Autowired
  private ObjectProvider<ExceptionReporter> exceptionReporter;

  @Test
  void rolePage() {
    assertPage(roleService.page(req()));
  }

  @Test
  void filePage() {
    assertPage(fileService.page(req()));
  }

  @Test
  void oauthClientPage() {
    assertPage(oauthClientService.page(req()));
  }

  @Test
  void deptList() {
    List<SysDeptVo> list = deptService.list(null, null);
    assertNotNull(list);
  }

  @Test
  void onlinePage() {
    SysUserOnlineQueryBo q = new SysUserOnlineQueryBo();
    q.setPageNum(1);
    q.setPageSize(10);
    assertNotNull(onlineService.page(q));
    assertNotNull(onlineService.page(q).getRecords());
  }

  @Test
  void genTablePage() {
    GenTableQueryBo q = new GenTableQueryBo();
    q.setPageNum(1);
    q.setPageSize(10);
    PageInfo<GenTableVo> page = genTableService.page(q);
    assertNotNull(page);
    assertNotNull(page.getRecords());
  }

  @Test
  void exceptionReporterSpiPresent() {
    assertNotNull(exceptionReporter.getIfAvailable(), "monitor 应注册 ExceptionReporter 实现");
  }

  @Test
  void oauthClientFindByClientIdVoOnly() {
    PageInfo<SysOauthClientVo> page = oauthClientService.page(req());
    assertNotNull(page);
    if (!page.getRecords().isEmpty()) {
      String clientId = page.getRecords().get(0).getClientId();
      SysOauthClientVo detail = oauthClientService.findByClientId(clientId);
      assertNotNull(detail);
      assertNotNull(detail.getClientId());
    }
  }

  @Test
  void roleExportSmoke() {
    assertNotNull(roleService.export(new SysRoleVo()));
  }

  @Test
  void fileClassifyListEnabled() {
    List<SysFileClassifyVo> enabled = fileClassifyService.listEnabled();
    assertNotNull(enabled);
  }

  @Test
  void genDbTablesListSmoke() {
    assertNotNull(genTableService.listDbTables(null, null));
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
