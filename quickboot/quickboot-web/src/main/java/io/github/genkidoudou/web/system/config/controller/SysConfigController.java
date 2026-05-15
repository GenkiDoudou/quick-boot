package io.github.genkidoudou.web.system.config.controller;

import cn.hutool.core.bean.BeanUtil;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.github.genkidoudou.web.system.config.domain.SysConfig;
import io.github.genkidoudou.web.system.config.dto.SysConfigBo;
import io.github.genkidoudou.web.system.config.dto.SysConfigExcelRow;
import io.github.genkidoudou.web.system.config.dto.SysConfigQueryBo;
import io.github.genkidoudou.web.system.config.dto.SysConfigVo;
import io.github.genkidoudou.web.system.config.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统参数控制器。
 */
@Tag(name = "系统参数管理")
@Validated
@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
public class SysConfigController {
    private final SysConfigService service;

    @Operation(summary = "参数列表")
    @GetMapping("/list")
    public R<List<SysConfigVo>> list(SysConfigQueryBo query) {
        List<SysConfig> rows = service.list(query);
        List<SysConfigVo> result = new ArrayList<>(rows.size());
        for (SysConfig row : rows) {
            result.add(BeanUtil.copyProperties(row, SysConfigVo.class));
        }
        return R.ok(result);
    }

    @Operation(summary = "参数详情")
    @GetMapping("/{configId}")
    public R<SysConfigVo> get(@Parameter(description = "参数ID") @PathVariable @Min(1) Long configId) {
        SysConfig row = service.getById(configId);
        if (row == null) {
            return R.ok();
        }
        return R.ok(BeanUtil.copyProperties(row, SysConfigVo.class));
    }

    @Operation(summary = "按参数键名查询参数值")
    @GetMapping("/configKey/{configKey}")
    public R<String> getByConfigKey(@Parameter(description = "参数键名") @PathVariable @NotBlank String configKey) {
        return R.ok(service.getConfigValueByKey(configKey));
    }

    @Operation(summary = "新增参数")
    @PostMapping("/create")
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SysConfigBo req) {
        service.add(req);
        return R.ok();
    }

    @Operation(summary = "修改参数")
    @PostMapping("/update")
    public R<Void> update(@Validated(UpdateGroup.class) @RequestBody SysConfigBo req) {
        service.update(req);
        return R.ok();
    }

    @Operation(summary = "删除参数")
    @PostMapping("/remove")
    public R<Void> remove(@RequestBody List<Long> configIds) {
        service.removeBatch(configIds);
        return R.ok();
    }

    @Operation(summary = "刷新参数缓存")
    @PostMapping("/refreshCache")
    public R<Void> refreshCache() {
        service.refreshCache();
        return R.ok();
    }

    @Operation(summary = "导出参数")
    @PostMapping("/export")
    public void export(SysConfigQueryBo query, HttpServletResponse response) {
        List<SysConfig> rows = service.list(query);
        List<SysConfigExcelRow> exportRows = new ArrayList<>(rows.size());
        for (SysConfig row : rows) {
            SysConfigExcelRow excelRow = BeanUtil.copyProperties(row, SysConfigExcelRow.class);
            excelRow.setConfigTypeName("1".equals(row.getConfigType()) ? "是" : "否");
            exportRows.add(excelRow);
        }
        ExcelUtils.exportExcel(exportRows, "sys-config", SysConfigExcelRow.class, response);
    }
}
