package io.github.genkidoudou.web.system.dict.data.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.bean.BeanUtil;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.excel.ExcelImportResult;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.github.genkidoudou.web.system.dict.data.domain.SysDictData;
import io.github.genkidoudou.web.system.dict.data.dto.SysDictDataBo;
import io.github.genkidoudou.web.system.dict.data.dto.SysDictDataExcelRow;
import io.github.genkidoudou.web.system.dict.data.dto.SysDictDataVo;
import io.github.genkidoudou.web.system.dict.data.service.DictDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Tag(name = "字典数据管理")
@Validated
@RestController
@RequestMapping("/system/dict/data")
@RequiredArgsConstructor
public class DictDataController {
    private final DictDataService service;

    @Operation(summary = "字典项列表")
    @SaCheckPermission("system:dict:list")
    @GetMapping("/list")
    public R<List<SysDictDataVo>> list(@RequestParam String dictType,
                                       @RequestParam(required = false) String dictLabel,
                                       @RequestParam(required = false) String status) {
        List<SysDictData> rows = service.list(dictType, dictLabel, status);
        List<SysDictDataVo> result = new ArrayList<>(rows.size());
        for (SysDictData row : rows) {
            result.add(BeanUtil.copyProperties(row, SysDictDataVo.class));
        }
        return R.ok(result);
    }

    @Operation(summary = "按类型查询字典项")
    @SaCheckPermission(value = {"system:dict:query", "system:dict:list"}, mode = SaMode.OR)
    @GetMapping("/type/{dictType}")
    public R<List<SysDictDataVo>> byType(@PathVariable String dictType) {
        List<SysDictData> rows = service.listByType(dictType);
        List<SysDictDataVo> result = new ArrayList<>(rows.size());
        for (SysDictData row : rows) {
            result.add(BeanUtil.copyProperties(row, SysDictDataVo.class));
        }
        return R.ok(result);
    }

    @Operation(summary = "字典项详情")
    @SaCheckPermission("system:dict:query")
    @GetMapping("/{dictCode}")
    public R<SysDictDataVo> get(@PathVariable @Min(1) Long dictCode) {
        SysDictData row = service.getById(dictCode);
        if (row == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "字典项不存在或已删除");
        }
        return R.ok(BeanUtil.copyProperties(row, SysDictDataVo.class));
    }

    @Operation(summary = "新增字典项")
    @SaCheckPermission("system:dict:add")
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SysDictDataBo req) {
        service.add(req);
        return R.ok();
    }

    @Operation(summary = "修改字典项")
    @SaCheckPermission("system:dict:edit")
    @PostMapping("/update")
    public R<Void> update(@Validated(UpdateGroup.class) @RequestBody SysDictDataBo req) {
        service.update(req);
        return R.ok();
    }

    @Operation(summary = "删除字典项")
    @SaCheckPermission("system:dict:remove")
    @PostMapping("/remove/{dictCode}")
    public R<Void> remove(@PathVariable @Min(1) Long dictCode) {
        service.remove(dictCode);
        return R.ok();
    }

    @Operation(summary = "导出字典项")
    @SaCheckPermission("system:dict:export")
    @PostMapping("/export")
    public void export(@RequestParam String dictType,
                       @RequestParam(required = false) String dictLabel,
                       @RequestParam(required = false) String status,
                       HttpServletResponse response) {
        List<SysDictData> rows = service.export(dictType, dictLabel, status);
        List<SysDictDataExcelRow> exportRows = new ArrayList<>(rows.size());
        for (SysDictData row : rows) {
            exportRows.add(BeanUtil.copyProperties(row, SysDictDataExcelRow.class));
        }
        ExcelUtils.exportExcel(exportRows, "dict-data", SysDictDataExcelRow.class, response);
    }

    @Operation(summary = "导入字典项")
    @SaCheckPermission("system:dict:import")
    @PostMapping("/import")
    public R<ExcelImportResult> importData(@RequestPart("file") MultipartFile file,
                                           @RequestParam String dictType,
                                           @RequestParam(defaultValue = "false") boolean updateSupport) {
        return R.ok(service.importData(file, dictType, updateSupport));
    }

    @Operation(summary = "下载字典项导入模板")
    @SaCheckPermission("system:dict:import")
    @PostMapping("/import/template")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtils.exportExcel(Collections.emptyList(), "dict-data-template", SysDictDataExcelRow.class, response);
    }
}
