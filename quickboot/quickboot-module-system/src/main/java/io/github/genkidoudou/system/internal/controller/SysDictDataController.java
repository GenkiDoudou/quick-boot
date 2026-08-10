package io.github.genkidoudou.system.internal.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.github.genkidoudou.system.internal.service.ISysDictDataService;
import io.github.genkidoudou.system.internal.vo.SysDictDataImportRow;
import io.github.genkidoudou.system.internal.vo.SysDictDataVo;
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

@Tag(name = "字典数据")
@RequiredArgsConstructor
@RestController
@RequestMapping("sys/dict/data")
public class SysDictDataController {

  private final ISysDictDataService service;

  @Operation(summary = "分页查询")
  @SaCheckPermission("system:dictData:list")
  @PostMapping("page")
  public R<PageInfo<SysDictDataVo>> page(@RequestBody PageRequest<SysDictDataVo> pageRequest) {
    return R.ok(service.page(pageRequest));
  }

  @Operation(summary = "字典数据详情")
  @SaCheckPermission(value = {"system:dictData:list", "system:dict:query", "system:dict:list"}, mode = SaMode.OR)
  @GetMapping("/{dictCode}")
  public R<SysDictDataVo> get(@PathVariable Long dictCode) {
    return R.ok(service.getDetail(dictCode));
  }

  /** 供 useDict；登录用户可访问 */
  @Operation(summary = "按类型查询字典数据")
  @GetMapping("type/{dictType}")
  public R<List<SysDictDataVo>> listByType(@PathVariable String dictType) {
    return R.ok(service.listByType(dictType));
  }

  @Operation(summary = "新增字典数据")
  @SaCheckPermission("system:dictData:add")
  @PostMapping("add")
  public R<String> add(@RequestBody @Validated(AddGroup.class) SysDictDataVo vo) {
    Long id = service.add(vo);
    return R.ok(id == null ? null : String.valueOf(id));
  }

  @Operation(summary = "修改字典数据")
  @SaCheckPermission("system:dictData:edit")
  @PostMapping("update")
  public R<Boolean> update(@RequestBody @Validated(UpdateGroup.class) SysDictDataVo vo) {
    return R.ok(service.update(vo));
  }

  @Operation(summary = "删除字典数据")
  @SaCheckPermission("system:dictData:remove")
  @GetMapping("remove/{dictCode}")
  public R<Void> removeGet(@PathVariable Long dictCode) {
    service.remove(List.of(dictCode));
    return R.ok();
  }

  @Operation(summary = "批量删除字典数据")
  @SaCheckPermission("system:dictData:remove")
  @PostMapping("remove")
  public R<Void> remove(@RequestBody List<Long> ids) {
    service.remove(ids);
    return R.ok();
  }

  @Operation(summary = "导出字典数据")
  @SaCheckPermission("system:dictData:export")
  @PostMapping("export")
  public void export(@RequestBody(required = false) SysDictDataVo query, HttpServletResponse response) throws Exception {
    ExcelUtils.exportExcel(service.export(query), "字典数据", SysDictDataVo.class, response);
  }

  @Operation(summary = "导入模板")
  @SaCheckPermission("system:dictData:import")
  @GetMapping("import/template")
  public void template(HttpServletResponse response) {
    ExcelUtils.exportExcel(Collections.emptyList(), "dict-data-import-template", SysDictDataImportRow.class, false, true, response);
  }

  @Operation(summary = "导入字典数据")
  @SaCheckPermission("system:dictData:import")
  @PostMapping("import")
  public R<ExcelResult<SysDictDataImportRow>> importExcel(
    @RequestParam("file") MultipartFile file,
    @RequestParam(value = "updateSupport", defaultValue = "false") String updateSupport) throws IOException {
    boolean update = "true".equalsIgnoreCase(updateSupport) || "1".equals(updateSupport);
    return R.ok(service.importExcel(file, update));
  }
}
