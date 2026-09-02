package io.github.genkidoudou.system.internal.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.idempotency.Idempotent;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.monitor.operlog.IgnoreLogger;
import io.github.genkidoudou.system.internal.service.ISysLogininforService;
import io.github.genkidoudou.system.internal.support.LoginLockSupport;
import io.github.genkidoudou.system.internal.vo.SysLogininforVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 登录访问日志（监控）管理接口。
 */
@Tag(name = "登录日志")
@RequiredArgsConstructor
@RestController
@RequestMapping("monitor/logininfor")
public class SysLogininforController {

  private final ISysLogininforService service;
  private final LoginLockSupport loginLockSupport;

  /**
   * 分页查询。
   *
   * @param pageRequest 分页参数
   * @return 分页结果
   */
  @Operation(summary = "登录日志分页")
  @SaCheckPermission("monitor:logininfor:list")
  @PostMapping("page")
  public R<PageInfo<SysLogininforVo>> page(@RequestBody PageRequest<SysLogininforVo> pageRequest) {
    return R.ok(service.page(pageRequest));
  }

  /**
   * 批量删除。
   *
   * @param ids 主键列表
   * @return 空
   */
  @Operation(summary = "删除登录日志")
  @SaCheckPermission("monitor:logininfor:remove")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':rm:' + #ids", message = "请勿重复提交")
  @PostMapping("/remove")
  public R<Void> remove(@RequestBody List<Long> ids) {
    service.remove(ids);
    return R.ok();
  }

  /**
   * 清空全部。
   *
   * @return 空
   */
  @Operation(summary = "清空登录日志")
  @SaCheckPermission("monitor:logininfor:remove")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':clean:logininfor'", message = "请勿重复提交")
  @PostMapping("/clean")
  public R<Void> clean() {
    service.cleanAll();
    return R.ok();
  }

  /**
   * 同步导出（不记录 RESULT）。
   *
   * @param request  条件或 ids
   * @param response 文件流
   */
  @Operation(summary = "导出登录日志")
  @IgnoreLogger(type = IgnoreLogger.Type.RESULT)
  @SaCheckPermission("monitor:logininfor:export")
  @PostMapping("/export")
  public void export(@RequestBody(required = false) SysLogininforVo request, HttpServletResponse response) {
    List<SysLogininforVo> export = service.export(request);
    ExcelUtils.exportExcel(export, "登录日志", SysLogininforVo.class, response);
  }

  /**
   * 清除指定用户名的登录失败锁定缓存。
   *
   * @param userName 登录用户名
   * @return 空
   */
  @Operation(summary = "账户解锁")
  @SaCheckPermission("monitor:logininfor:unlock")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':unlock:' + #userName", message = "请勿重复提交")
  @GetMapping("/unlock/{userName}")
  public R<Void> unlock(@Parameter(description = "登录用户名") @PathVariable String userName) {
    String name = StrUtil.trim(userName);
    if (StrUtil.isBlank(name)) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "用户名不能为空");
    }
    loginLockSupport.clear(name);
    return R.ok();
  }
}
