package io.github.genkidoudou.web.system.dict.data.controller;

import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.system.dict.data.domain.SysDictData;
import io.github.genkidoudou.web.system.dict.data.dto.SysDictDataSaveRequest;
import io.github.genkidoudou.web.system.dict.data.service.DictDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "字典项管理")
@Validated
@RestController
@RequestMapping("/system/dict/data")
@RequiredArgsConstructor
public class DictDataController {
    private final DictDataService service;

    @Operation(summary = "字典项列表")
    @GetMapping("/list")
    public R<List<SysDictData>> list(@RequestParam String dictType,
                                     @RequestParam(required = false) String dictLabel,
                                     @RequestParam(required = false) String status) {
        return R.ok(service.list(dictType, dictLabel, status));
    }

    @Operation(summary = "按类型查询字典项")
    @GetMapping("/type/{dictType}")
    public R<List<SysDictData>> byType(@PathVariable String dictType) {
        return R.ok(service.listByType(dictType));
    }

    @Operation(summary = "字典项详情")
    @GetMapping("/{dictCode}")
    public R<SysDictData> get(@PathVariable @Min(1) Long dictCode) {
        SysDictData row = service.getById(dictCode);
        if (row == null) throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "字典项不存在或已删除");
        return R.ok(row);
    }

    @Operation(summary = "新增字典项")
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysDictDataSaveRequest req) { service.add(req); return R.ok(); }

    @Operation(summary = "修改字典项")
    @PostMapping("/update")
    public R<Void> update(@Valid @RequestBody SysDictDataSaveRequest req) { service.update(req); return R.ok(); }

    @Operation(summary = "删除字典项")
    @PostMapping("/remove/{dictCode}")
    public R<Void> remove(@PathVariable @Min(1) Long dictCode) { service.remove(dictCode); return R.ok(); }

    @Operation(summary = "导出字典项")
    @PostMapping("/export")
    public R<List<SysDictData>> export(@RequestBody SysDictDataSaveRequest req) {
        return R.ok(service.export(req.getDictType(), req.getDictLabel(), req.getStatus()));
    }
}
