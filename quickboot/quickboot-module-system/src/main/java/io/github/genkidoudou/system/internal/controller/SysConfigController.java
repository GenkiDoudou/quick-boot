package io.github.genkidoudou.system.internal.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.common.monitor.operlog.IgnoreLogger;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.github.genkidoudou.system.internal.service.ISysConfigService;
import io.github.genkidoudou.system.internal.vo.SysConfigImportRow;
import io.github.genkidoudou.system.internal.vo.SysConfigVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Tag(name = "参数配置")
@RequiredArgsConstructor
@RestController
@RequestMapping("sys/config")
public class SysConfigController {

  private final ISysConfigService service;

  @Operation(summary = "分页查询")
  @SaCheckPermission("system:config:list")
  @PostMapping("page")
  public R<PageInfo<SysConfigVo>> page(@RequestBody PageRequest<SysConfigVo> pageRequest) {
    return R.ok(service.page(pageRequest));
  }

  @Operation(summary = "参数详情")
  @SaCheckPermission(value = {"system:config:query", "system:config:list"}, mode = SaMode.OR)
  @GetMapping("/{configId}")
  public R<SysConfigVo> get(@PathVariable Long configId) {
    return R.ok(service.getDetail(configId));
  }

  @Operation(summary = "按键名查询值")
  @SaCheckPermission(value = {"system:config:query", "system:config:list"}, mode = SaMode.OR)
  @GetMapping("/configKey/{configKey}")
  public R<String> getByKey(@PathVariable String configKey) {
    return R.ok(service.getConfigValueByKey(configKey));
  }

  @Operation(summary = "新增参数")
  @SaCheckPermission("system:config:add")
  @PostMapping("add")
  public R<String> add(@RequestBody @Validated(AddGroup.class) SysConfigVo vo) {
    Long id = service.add(vo);
    return R.ok(id == null ? null : String.valueOf(id));
  }

  @Operation(summary = "修改参数")
  @SaCheckPermission("system:config:edit")
  @PostMapping("update")
  public R<Boolean> update(@RequestBody @Validated(UpdateGroup.class) SysConfigVo vo) {
    return R.ok(service.update(vo));
  }

  @Operation(summary = "删除参数")
  @SaCheckPermission("system:config:remove")
  @GetMapping("remove/{configId}")
  public R<Void> removeGet(@PathVariable Long configId) {
    service.remove(List.of(configId));
    return R.ok();
  }

  @Operation(summary = "批量删除参数")
  @SaCheckPermission("system:config:remove")
  @PostMapping("/remove")
  public R<Void> remove(@RequestBody List<Long> ids) {
    service.remove(ids);
    return R.ok();
  }

  @Operation(summary = "刷新参数缓存")
  @SaCheckPermission(value = {"system:config:query", "system:config:list"}, mode = SaMode.OR)
  @PostMapping("/refreshCache")
  public R<Void> refreshCache() {
    service.refreshCache();
    return R.ok();
  }

  @Operation(summary = "导出参数")
  @IgnoreLogger(type = IgnoreLogger.Type.RESULT)
  @SaCheckPermission("system:config:export")
  @PostMapping("/export")
  public void export(@RequestBody(required = false) SysConfigVo request, HttpServletResponse response) {
    List<SysConfigVo> export = service.export(request);
    ExcelUtils.exportExcel(export, "参数配置", SysConfigVo.class, response);
  }

  @Operation(summary = "导入模板")
  @IgnoreLogger(type = IgnoreLogger.Type.RESULT)
  @SaCheckPermission("system:config:import")
  @GetMapping("/import/template")
  public void importTemplate(HttpServletResponse response) {
    ExcelUtils.exportExcel(Collections.emptyList(), "config-import-template",
      SysConfigImportRow.class, false, true, response);
  }

  @Operation(summary = "导入参数")
  @SaCheckPermission("system:config:import")
  @PostMapping("/import")
  public R<ExcelResult<SysConfigImportRow>> importExcel(@RequestParam("file") MultipartFile file,
                                                        @RequestParam(value = "updateSupport", defaultValue = "false")
                                                        String updateSupport) throws IOException {
    boolean update = "true".equalsIgnoreCase(updateSupport) || "1".equals(updateSupport);
    return R.ok(service.importExcel(file, update));
  }
}
