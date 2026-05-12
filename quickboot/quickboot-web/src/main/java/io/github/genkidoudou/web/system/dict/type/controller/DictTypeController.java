package io.github.genkidoudou.web.system.dict.type.controller;

import cn.hutool.core.bean.BeanUtil;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
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

  @Operation(summary = "字典类型列表")
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
  @GetMapping("/{dictId}")
  public R<SysDictTypeVo> get(@PathVariable @Min(1) Long dictId) {
    SysDictType row = service.getById(dictId);
    if (row == null) {
      throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "字典类型不存在或已删除");
    }
    return R.ok(BeanUtil.copyProperties(row, SysDictTypeVo.class));
  }

  @Operation(summary = "新增字典类型")
  @PostMapping
  public R<Void> add(@Validated(AddGroup.class) @RequestBody SysDictTypeBo req) {
    service.add(req);
    return R.ok();
  }

  @Operation(summary = "修改字典类型")
  @PostMapping("/update")
  public R<Void> update(@Validated(UpdateGroup.class) @RequestBody SysDictTypeBo req) {
    service.update(req);
    return R.ok();
  }

  @Operation(summary = "删除字典类型")
  @PostMapping("/remove/{dictId}")
  public R<Void> remove(@PathVariable @Min(1) Long dictId) {
    service.remove(dictId);
    return R.ok();
  }

  @Operation(summary = "导出字典类型")
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
  @PostMapping("/refresh")
  public R<Void> refreshAll() {
    service.refreshAllCache();
    return R.ok();
  }

  @Operation(summary = "刷新单个字典缓存")
  @PostMapping("/refresh/{dictType}")
  public R<Void> refresh(@Parameter(description = "字典类型") @PathVariable String dictType) {
    service.refreshTypeCache(dictType);
    return R.ok();
  }

  @Operation(summary = "导入字典类型")
  @PostMapping("/import")
  public R<ExcelImportResult> importData(@RequestPart("file") MultipartFile file,
                                         @RequestParam(defaultValue = "false") boolean updateSupport) throws IOException {
    return R.ok(service.importData(file, updateSupport));
  }

  @Operation(summary = "下载字典类型导入模板")
  @PostMapping("/import/template")
  public void importTemplate(HttpServletResponse response) {
    ExcelUtils.exportExcel(Collections.emptyList(), "dict-type-template", SysDictTypeExcelRow.class, response);
  }
}
