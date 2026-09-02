package io.github.genkidoudou.web.system;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.system.internal.service.ISysConfigService;
import io.github.genkidoudou.system.internal.service.ISysLogininforService;
import io.github.genkidoudou.system.internal.service.ISysOperLogService;
import io.github.genkidoudou.system.internal.service.ISysPermissionService;
import io.github.genkidoudou.system.internal.vo.SysConfigVo;
import io.github.genkidoudou.system.internal.vo.SysLogininforVo;
import io.github.genkidoudou.system.internal.vo.SysOperLogVo;
import io.github.genkidoudou.web.support.QuickbootIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 全栈冒烟（对应 tasks 9.3）：菜单路由 → Config CRUD/导出 → 登录日志写入与分页 → 操作日志分页。
 * <p>
 * 不走完整 HTTP OAuth 登录态；覆盖登录后管理端核心域 Service 契约（与浏览器冒烟清单对齐）。
 */
@Transactional
class LoginAndOperLogIT extends QuickbootIntegrationTestBase {

  @Autowired
  private ISysPermissionService permissionService;

  @Autowired
  private ISysConfigService configService;

  @Autowired
  private ISysLogininforService logininforService;

  @Autowired
  private ISysOperLogService operLogService;

  @Test
  void smokeMenuRoutesAvailable() {
    List<Map<String, Object>> routes = permissionService.buildRouters("1");
    assertNotNull(routes);
    assertFalse(routes.isEmpty(), "登录后动态菜单不可为空");
  }

  @Test
  void smokeConfigCrudAndExport() {
    PageRequest<SysConfigVo> pageRequest = new PageRequest<>();
    pageRequest.setCurrent(1);
    pageRequest.setSize(10);
    PageInfo<SysConfigVo> page = configService.page(pageRequest);
    assertNotNull(page);
    assertNotNull(page.getRecords());

    SysConfigVo vo = new SysConfigVo();
    vo.setConfigName("冒烟测试参数");
    vo.setConfigKey("it.smoke.config." + System.nanoTime());
    vo.setConfigValue("1");
    vo.setConfigType("0");
    Long id = configService.add(vo);
    assertNotNull(id);
    assertNotNull(configService.getDetail(id));

    List<SysConfigVo> exported = configService.export(new SysConfigVo());
    assertNotNull(exported);
    assertTrue(exported.size() >= 1, "导出列表至少含种子或新建参数");
  }

  @Test
  void smokeLogininforRecordAndPage() {
    logininforService.record("smoke_user", 1L, "test-client", "127.0.0.1",
      "IT-Agent", "0", "冒烟登录成功");
    PageRequest<SysLogininforVo> req = new PageRequest<>();
    req.setCurrent(1);
    req.setSize(20);
    SysLogininforVo param = new SysLogininforVo();
    param.setUserName("smoke_user");
    req.setParam(param);
    PageInfo<SysLogininforVo> page = logininforService.page(req);
    assertNotNull(page);
    assertNotNull(page.getRecords());
    assertTrue(page.getRecords().stream().anyMatch(r -> "smoke_user".equals(r.getUserName())),
      "应能查到刚写入的登录日志");
  }

  @Test
  void smokeOperLogPage() {
    PageRequest<SysOperLogVo> req = new PageRequest<>();
    req.setCurrent(1);
    req.setSize(10);
    PageInfo<SysOperLogVo> page = operLogService.page(req);
    assertNotNull(page);
    assertNotNull(page.getRecords());
  }

  @Test
  void smokeOperLogExport() {
    List<SysOperLogVo> rows = operLogService.export(new SysOperLogVo());
    assertNotNull(rows);
  }

  @Test
  void smokeLogininforExport() {
    List<SysLogininforVo> rows = logininforService.export(new SysLogininforVo());
    assertNotNull(rows);
  }
}
