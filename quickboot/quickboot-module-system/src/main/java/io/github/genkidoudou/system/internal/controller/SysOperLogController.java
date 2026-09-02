package io.github.genkidoudou.system.internal.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.idempotency.Idempotent;
import io.github.genkidoudou.common.monitor.operlog.IgnoreLogger;
import io.github.genkidoudou.system.internal.service.ISysOperLogService;
import io.github.genkidoudou.system.internal.vo.SysOperLogVo;
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
 * 操作日志（监控）管理接口；类级忽略采集，避免自递归噪声。
 */
@Tag(name = "操作日志")
@IgnoreLogger(type = IgnoreLogger.Type.ALL)
@RequiredArgsConstructor
@RestController
@RequestMapping("monitor/operlog")
public class SysOperLogController {

  private final ISysOperLogService service;

  /**
   * 分页查询。
   *
   * @param pageRequest 分页参数
   * @return 分页结果
   */
  @Operation(summary = "操作日志分页")
  @SaCheckPermission("monitor:operlog:list")
  @PostMapping("page")
  public R<PageInfo<SysOperLogVo>> page(@RequestBody PageRequest<SysOperLogVo> pageRequest) {
    return R.ok(service.page(pageRequest));
  }

  /**
   * 详情。
   *
   * @param operId 主键
   * @return 详情
   */
  @Operation(summary = "操作日志详情")
  @SaCheckPermission(value = {"monitor:operlog:query", "monitor:operlog:list"}, mode = SaMode.OR)
  @GetMapping("/{operId}")
  public R<SysOperLogVo> get(@Parameter(description = "日志主键") @PathVariable String operId) {
    return R.ok(service.getDetail(Long.parseLong(operId)));
  }

  /**
   * 批量删除。
   *
   * @param ids 主键列表
   * @return 空
   */
  @Operation(summary = "删除操作日志")
  @SaCheckPermission("monitor:operlog:remove")
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
  @Operation(summary = "清空操作日志")
  @SaCheckPermission("monitor:operlog:remove")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':clean:operlog'", message = "请勿重复提交")
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
  @Operation(summary = "导出操作日志")
  @IgnoreLogger(type = IgnoreLogger.Type.RESULT)
  @SaCheckPermission("monitor:operlog:export")
  @PostMapping("/export")
  public void export(@RequestBody(required = false) SysOperLogVo request, HttpServletResponse response) {
    List<SysOperLogVo> export = service.export(request);
    ExcelUtils.exportExcel(export, "操作日志", SysOperLogVo.class, response);
  }
}
