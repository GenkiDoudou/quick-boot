package io.github.genkidoudou.tool.internal.gen.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.api.PageRequestMapping;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.web.DeprecatedApiSupport;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.monitor.operlog.IgnoreLogger;
import io.github.genkidoudou.tool.internal.gen.dto.GenBatchCodeBo;
import io.github.genkidoudou.tool.internal.gen.dto.GenCreateTableBo;
import io.github.genkidoudou.tool.internal.gen.dto.GenDefaultsVo;
import io.github.genkidoudou.tool.internal.gen.dto.GenDbTableVo;
import io.github.genkidoudou.tool.internal.gen.dto.GenImportTableBo;
import io.github.genkidoudou.tool.internal.gen.dto.GenPreviewVo;
import io.github.genkidoudou.tool.internal.gen.dto.GenTableDetailVo;
import io.github.genkidoudou.tool.internal.gen.dto.GenTableQueryBo;
import io.github.genkidoudou.tool.internal.gen.dto.GenTableUpdateBo;
import io.github.genkidoudou.tool.internal.gen.dto.GenTableVo;
import io.github.genkidoudou.tool.internal.gen.service.GenTableService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * 代码生成接口。
 */
@Tag(name = "代码生成")
@Validated
@RestController
@RequestMapping("/tool/gen")
@RequiredArgsConstructor
@IgnoreLogger(type = IgnoreLogger.Type.ALL)
public class GenController {

    private final GenTableService genTableService;

    @Operation(summary = "生成配置分页列表")
    @SaCheckPermission("tool:gen:list")
    @PostMapping("/page")
    public R<PageInfo<GenTableVo>> page(@RequestBody PageRequest<GenTableQueryBo> pageRequest) {
        return R.ok(genTableService.page(toGenQuery(pageRequest)));
    }

    /**
     * @deprecated 请改用 POST {@code /tool/gen/page}
     */
    @Deprecated
    @Operation(summary = "生成配置分页列表（兼容）", deprecated = true)
    @SaCheckPermission("tool:gen:list")
    @GetMapping("/list")
    public R<PageInfo<GenTableVo>> list(HttpServletResponse response, @Validated GenTableQueryBo query) {
        DeprecatedApiSupport.markDeprecated(response);
        return R.ok(genTableService.page(query));
    }

    @Operation(summary = "数据库表候选列表")
    @SaCheckPermission("tool:gen:list")
    @GetMapping("/db/list")
    public R<List<GenDbTableVo>> dbList(
        @RequestParam(required = false) String tableName,
        @RequestParam(required = false) String tableComment
    ) {
        return R.ok(genTableService.listDbTables(tableName, tableComment));
    }

    @Operation(summary = "代码生成全局默认配置")
    @SaCheckPermission("tool:gen:list")
    @GetMapping("/defaults")
    public R<GenDefaultsVo> defaults() {
        return R.ok(genTableService.getDefaults());
    }

    @Operation(summary = "生成配置详情")
    @SaCheckPermission("tool:gen:list")
    @GetMapping("/{tableId}")
    public R<GenTableDetailVo> get(@Parameter(description = "表配置主键") @PathVariable @Min(1) Long tableId) {
        return R.ok(genTableService.getDetail(tableId));
    }

    @Operation(summary = "保存生成配置")
    @SaCheckPermission("tool:gen:edit")
    @PostMapping("/update")
    public R<Void> update(@Validated @RequestBody GenTableUpdateBo req) {
        genTableService.update(req);
        return R.ok();
    }

    @Operation(summary = "导入表")
    @SaCheckPermission("tool:gen:import")
    @PostMapping("/importTable")
    public R<Void> importTable(@Validated @RequestBody GenImportTableBo req) {
        genTableService.importTables(req.getTables());
        return R.ok();
    }

    @Operation(summary = "执行建表 SQL")
    @SaCheckPermission("tool:gen:create")
    @PostMapping("/createTable")
    public R<Void> createTable(@Validated @RequestBody GenCreateTableBo req) {
        genTableService.createTable(req);
        return R.ok();
    }

    @Operation(summary = "预览生成代码")
    @SaCheckPermission("tool:gen:preview")
    @GetMapping("/preview/{tableId}")
    public R<List<GenPreviewVo>> preview(@PathVariable @Min(1) Long tableId) {
        return R.ok(genTableService.preview(tableId));
    }

    @Operation(summary = "删除生成配置")
    @SaCheckPermission("tool:gen:remove")
    @PostMapping("/remove/{tableId}")
    public R<Void> remove(@PathVariable @Min(1) Long tableId) {
        genTableService.remove(tableId);
        return R.ok();
    }

    @Operation(summary = "同步库表结构")
    @SaCheckPermission("tool:gen:edit")
    @PostMapping("/synchDb/{tableName}")
    public R<Void> synchDb(@PathVariable @NotBlank String tableName) {
        genTableService.synchDb(tableName);
        return R.ok();
    }

    @Operation(summary = "批量下载生成代码 Zip")
    @SaCheckPermission("tool:gen:code")
    @PostMapping("/batchGenCode")
    public void batchGenCode(@Validated @RequestBody GenBatchCodeBo req, HttpServletResponse response) throws IOException {
        genTableService.batchGenCode(req.getTables(), response);
    }

    @Operation(summary = "自定义路径写盘")
    @SaCheckPermission("tool:gen:code")
    @PostMapping("/genCode/{tableName}")
    public R<String> genCode(@PathVariable @NotBlank String tableName) throws IOException {
        return R.ok(genTableService.genCodeToPath(tableName));
    }

    private static GenTableQueryBo toGenQuery(PageRequest<GenTableQueryBo> pageRequest) {
        GenTableQueryBo param = pageRequest != null && pageRequest.getParam() != null
            ? pageRequest.getParam()
            : new GenTableQueryBo();
        param.setPageNum(PageRequestMapping.pageNum(pageRequest));
        param.setPageSize(PageRequestMapping.pageSize(pageRequest));
        return param;
    }
}
