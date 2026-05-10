package io.github.genkidoudou.web.system.dict.type.controller;

import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.system.dict.type.domain.SysDictType;
import io.github.genkidoudou.web.system.dict.type.dto.SysDictTypeSaveRequest;
import io.github.genkidoudou.web.system.dict.type.service.DictTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public R<List<SysDictType>> list(@RequestParam(required = false) String dictName,
                                     @RequestParam(required = false) String dictType,
                                     @RequestParam(required = false) String status) {
        return R.ok(service.list(dictName, dictType, status));
    }

    @Operation(summary = "字典类型详情")
    @GetMapping("/{dictId}")
    public R<SysDictType> get(@PathVariable @Min(1) Long dictId) {
        SysDictType row = service.getById(dictId);
        if (row == null) throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "字典类型不存在或已删除");
        return R.ok(row);
    }

    @Operation(summary = "新增字典类型")
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysDictTypeSaveRequest req) { service.add(req); return R.ok(); }

    @Operation(summary = "修改字典类型")
    @PostMapping("/update")
    public R<Void> update(@Valid @RequestBody SysDictTypeSaveRequest req) { service.update(req); return R.ok(); }

    @Operation(summary = "删除字典类型")
    @PostMapping("/remove/{dictId}")
    public R<Void> remove(@PathVariable @Min(1) Long dictId) { service.remove(dictId); return R.ok(); }

    @Operation(summary = "导出字典类型")
    @PostMapping("/export")
    public R<List<SysDictType>> export(@RequestBody(required = false) SysDictTypeSaveRequest req) {
        String dictName = req != null ? req.getDictName() : null;
        String dictType = req != null ? req.getDictType() : null;
        String status = req != null ? req.getStatus() : null;
        return R.ok(service.export(dictName, dictType, status));
    }

    @Operation(summary = "刷新全部字典缓存")
    @PostMapping("/refresh")
    public R<Void> refreshAll() { service.refreshAllCache(); return R.ok(); }

    @Operation(summary = "刷新单个字典缓存")
    @PostMapping("/refresh/{dictType}")
    public R<Void> refresh(@Parameter(description = "字典类型") @PathVariable String dictType) {
        service.refreshTypeCache(dictType);
        return R.ok();
    }
}
