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
import io.github.genkidoudou.system.internal.service.ISysDictTypeService;
import io.github.genkidoudou.system.internal.vo.SysDictTypeImportRow;
import io.github.genkidoudou.system.internal.vo.SysDictTypeVo;
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

@Tag(name = "字典类型")
@RequiredArgsConstructor
@RestController
@RequestMapping("sys/dict/type")
public class SysDictTypeController {

  private final ISysDictTypeService service;

  @Operation(summary = "分页查询")
  @SaCheckPermission("system:dict:list")
  @PostMapping("page")
  public R<PageInfo<SysDictTypeVo>> page(@RequestBody PageRequest<SysDictTypeVo> pageRequest) {
    return R.ok(service.page(pageRequest));
  }

  @Operation(summary = "字典类型详情")
  @SaCheckPermission(value = {"system:dict:query", "system:dict:list"}, mode = SaMode.OR)
  @GetMapping("/{dictId}")
  public R<SysDictTypeVo> get(@PathVariable Long dictId) {
    return R.ok(service.getDetail(dictId));
  }

  @Operation(summary = "新增字典类型")
  @SaCheckPermission("system:dict:add")
  @PostMapping("add")
  public R<String> add(@RequestBody @Validated(AddGroup.class) SysDictTypeVo vo) {
    Long id = service.add(vo);
    return R.ok(id == null ? null : String.valueOf(id));
  }

  @Operation(summary = "修改字典类型")
  @SaCheckPermission("system:dict:edit")
  @PostMapping("update")
  public R<Boolean> update(@RequestBody @Validated(UpdateGroup.class) SysDictTypeVo vo) {
    return R.ok(service.update(vo));
  }

  @Operation(summary = "删除字典类型")
  @SaCheckPermission("system:dict:remove")
  @GetMapping("remove/{dictId}")
  public R<Void> removeGet(@PathVariable Long dictId) {
    service.remove(List.of(dictId));
    return R.ok();
  }

  @Operation(summary = "批量删除字典类型")
  @SaCheckPermission("system:dict:remove")
  @PostMapping("remove")
  public R<Void> remove(@RequestBody List<Long> ids) {
    service.remove(ids);
    return R.ok();
  }

  @Operation(summary = "刷新全部字典缓存")
  @SaCheckPermission("system:dict:refresh")
  @PostMapping("refresh")
  public R<Void> refreshAll() {
    service.refreshAll();
    return R.ok();
  }

  @Operation(summary = "刷新指定字典缓存")
  @SaCheckPermission("system:dict:refresh")
  @PostMapping("refresh/{dictType}")
  public R<Void> refresh(@PathVariable String dictType) {
    service.refresh(dictType);
    return R.ok();
  }

  @Operation(summary = "导出字典类型")
  @IgnoreLogger(type = IgnoreLogger.Type.RESULT)
  @SaCheckPermission("system:dict:export")
  @PostMapping("export")
  public void export(@RequestBody(required = false) SysDictTypeVo query, HttpServletResponse response) throws Exception {
    ExcelUtils.exportExcel(service.export(query), "字典类型", SysDictTypeVo.class, response);
  }

  @Operation(summary = "导入模板")
  @IgnoreLogger(type = IgnoreLogger.Type.RESULT)
  @SaCheckPermission("system:dict:import")
  @GetMapping("import/template")
  public void template(HttpServletResponse response) {
    ExcelUtils.exportExcel(Collections.emptyList(), "dict-type-import-template", SysDictTypeImportRow.class, false, true, response);
  }

  @Operation(summary = "导入字典类型")
  @SaCheckPermission("system:dict:import")
  @PostMapping("import")
  public R<ExcelResult<SysDictTypeImportRow>> importExcel(
    @RequestParam("file") MultipartFile file,
    @RequestParam(value = "updateSupport", defaultValue = "false") String updateSupport) throws IOException {
    boolean update = "true".equalsIgnoreCase(updateSupport) || "1".equals(updateSupport);
    return R.ok(service.importExcel(file, update));
  }
}
