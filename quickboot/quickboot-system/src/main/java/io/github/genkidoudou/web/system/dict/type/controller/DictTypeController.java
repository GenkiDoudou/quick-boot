package io.github.genkidoudou.web.system.dict.type.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.excel.ExcelImportResult;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.github.genkidoudou.web.system.dict.type.domain.SysDictType;
import io.github.genkidoudou.web.system.dict.type.dto.SysDictTypeBo;
import io.github.genkidoudou.web.system.dict.type.dto.SysDictTypeExcelRow;
import io.github.genkidoudou.web.system.dict.type.dto.SysDictTypeVo;
import io.github.genkidoudou.web.system.dict.type.service.DictTypeService;
import io.github.genkidoudou.web.system.importtask.dto.ImportSubmitResultVo;
import io.github.genkidoudou.web.system.importtask.handler.impl.DictTypeBizImportHandler;
import io.github.genkidoudou.web.system.importtask.service.ImportOrchestratorService;
import io.github.genkidoudou.web.system.importtask.support.ImportMode;
import io.github.genkidoudou.web.system.importtask.support.ImportSubmitMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Tag(name = "字典类型管理")
@Validated
@RestController
@RequestMapping("/system/dict/type")
@RequiredArgsConstructor
public class DictTypeController {
  private final DictTypeService service;
  private final ImportOrchestratorService importOrchestratorService;

  @Operation(summary = "字典类型列表")
  @SaCheckPermission("system:dict:list")
  @GetMapping("/list")
  public R<List<SysDictTypeVo>> list(@RequestParam(required = false) String dictName,
                                     @RequestParam(required = false) String dictType,
                                     @RequestParam(required = false) String status) {
    List<SysDictType> rows = service.list(dictName, dictType, status);
    List<SysDictTypeVo> result = new ArrayList<>(rows.size());
    for (SysDictType row : rows) {
      result.add(BeanUtil.copyProperties(row, SysDictTypeVo.class));
    }
    return R.ok(result);
  }

  @Operation(summary = "字典类型详情")
  @SaCheckPermission("system:dict:query")
  @GetMapping("/{dictId}")
  public R<SysDictTypeVo> get(@PathVariable @Min(1) Long dictId) {
    SysDictType row = service.getById(dictId);
    if (row == null) {
      throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "字典类型不存在或已删除");
    }
    return R.ok(BeanUtil.copyProperties(row, SysDictTypeVo.class));
  }

  @Operation(summary = "新增字典类型")
  @SaCheckPermission("system:dict:add")
  @PostMapping
  public R<Void> add(@Validated(AddGroup.class) @RequestBody SysDictTypeBo req) {
    service.add(req);
    return R.ok();
  }

  @Operation(summary = "修改字典类型")
  @SaCheckPermission("system:dict:edit")
  @PostMapping("/update")
  public R<Void> update(@Validated(UpdateGroup.class) @RequestBody SysDictTypeBo req) {
    service.update(req);
    return R.ok();
  }

  @Operation(summary = "删除字典类型")
  @SaCheckPermission("system:dict:remove")
  @PostMapping("/remove/{dictId}")
  public R<Void> remove(@PathVariable @Min(1) Long dictId) {
    service.remove(dictId);
    return R.ok();
  }

  @Operation(summary = "导出字典类型")
  @SaCheckPermission("system:dict:export")
  @PostMapping("/export")
  public void export(@RequestParam(required = false) String dictName,
                     @RequestParam(required = false) String dictType,
                     @RequestParam(required = false) String status,
                     HttpServletResponse response) {
    List<SysDictType> rows = service.export(dictName, dictType, status);
    List<SysDictTypeExcelRow> exportRows = new ArrayList<>(rows.size());
    for (SysDictType row : rows) {
      exportRows.add(BeanUtil.copyProperties(row, SysDictTypeExcelRow.class));
    }
    ExcelUtils.exportExcel(exportRows, "dict-type", SysDictTypeExcelRow.class, response);
  }

  @Operation(summary = "刷新全部字典缓存")
  @SaCheckPermission("system:dict:refresh")
  @PostMapping("/refresh")
  public R<Void> refreshAll() {
    service.refreshAllCache();
    return R.ok();
  }

  @Operation(summary = "刷新单个字典缓存")
  @SaCheckPermission("system:dict:refresh")
  @PostMapping("/refresh/{dictType}")
  public R<Void> refresh(@Parameter(description = "字典类型") @PathVariable String dictType) {
    service.refreshTypeCache(dictType);
    return R.ok();
  }

  @Operation(summary = "导入字典类型（默认异步编排，可在导入导出中心查看进度）")
  @SaCheckPermission("system:dict:import")
  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public R<ExcelImportResult> importData(@RequestPart("file") MultipartFile file,
                                         @RequestParam(defaultValue = "false") boolean updateSupport,
                                         @RequestParam(required = false) String mode,
                                         @RequestParam(required = false) Integer syncMaxRows) {
    String effectiveMode = StrUtil.isNotBlank(mode) ? mode.trim() : ImportMode.ASYNC;
    ImportSubmitResultVo submitted = importOrchestratorService.submit(
        file, DictTypeBizImportHandler.BIZ_TYPE, updateSupport, effectiveMode, syncMaxRows, null);
    return R.ok(ImportSubmitMapper.toExcelImportResult(submitted));
  }

  @Operation(summary = "下载字典类型导入模板")
  @SaCheckPermission("system:dict:import")
  @PostMapping("/import/template")
  public void importTemplate(HttpServletResponse response) {
    ExcelUtils.exportExcel(Collections.emptyList(), "dict-type-template", SysDictTypeExcelRow.class, response);
  }
}
