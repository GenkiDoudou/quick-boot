package io.github.genkidoudou.web.knowledge.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.github.genkidoudou.web.knowledge.dto.KbMcpOptionVo;
import io.github.genkidoudou.web.knowledge.dto.KbMcpServerBo;
import io.github.genkidoudou.web.knowledge.dto.KbMcpServerQueryBo;
import io.github.genkidoudou.web.knowledge.dto.KbMcpServerVo;
import io.github.genkidoudou.web.knowledge.dto.McpTestResultVo;
import io.github.genkidoudou.web.knowledge.service.KbMcpServerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 外部 MCP 管理接口。
 */
@Tag(name = "MCP 管理")
@Validated
@RestController
@RequestMapping("/ai/mcp")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "qc.knowledge.mcp", name = "enabled", havingValue = "true")
public class KbMcpServerController {

    private final KbMcpServerService service;

    @Operation(summary = "MCP 分页列表")
    @SaCheckPermission("ai:mcp:list")
    @GetMapping("/list")
    public R<PageInfo<KbMcpServerVo>> list(@Validated KbMcpServerQueryBo query) {
        return R.ok(service.page(query));
    }

    @Operation(summary = "MCP 详情")
    @SaCheckPermission("ai:mcp:query")
    @GetMapping("/getInfo")
    public R<KbMcpServerVo> getInfo(
        @Parameter(description = "MCP ID") @RequestParam @Min(1) Long mcpId,
        @Parameter(description = "是否展示密钥明文") @RequestParam(defaultValue = "false") boolean revealSecrets) {
        KbMcpServerVo vo = service.getInfo(mcpId, revealSecrets);
        if (vo == null) {
            return R.ok();
        }
        return R.ok(vo);
    }

    @Operation(summary = "新增 MCP")
    @SaCheckPermission("ai:mcp:add")
    @PostMapping("/add")
    public R<Void> add(@Validated(AddGroup.class) @RequestBody KbMcpServerBo req) {
        service.add(req);
        return R.ok();
    }

    @Operation(summary = "修改 MCP")
    @SaCheckPermission("ai:mcp:edit")
    @PostMapping("/update")
    public R<Void> update(@Validated(UpdateGroup.class) @RequestBody KbMcpServerBo req) {
        service.update(req);
        return R.ok();
    }

    @Operation(summary = "删除 MCP")
    @SaCheckPermission("ai:mcp:remove")
    @PostMapping("/remove")
    public R<Void> remove(@RequestBody List<Long> mcpIds) {
        service.removeBatch(mcpIds);
        return R.ok();
    }

    @Operation(summary = "MCP 连接测试")
    @SaCheckPermission("ai:mcp:test")
    @PostMapping("/test")
    public R<McpTestResultVo> test(
        @Parameter(description = "MCP ID") @RequestParam @Min(1) Long mcpId) {
        return R.ok(service.test(mcpId));
    }

    @Operation(summary = "导出 mcp.json 片段")
    @SaCheckPermission("ai:mcp:export")
    @GetMapping("/export")
    public R<Map<String, Object>> export(
        @Parameter(description = "逗号分隔的 MCP ID，缺省导出全部启用项") @RequestParam(required = false) String ids,
        @Parameter(description = "是否包含明文密钥") @RequestParam(defaultValue = "false") boolean includeSecrets) {
        List<Long> mcpIds = parseIds(ids);
        return R.ok(service.export(mcpIds, includeSecrets));
    }

    @Operation(summary = "MCP 下拉选项")
    @SaCheckPermission("ai:mcp:list")
    @GetMapping("/options")
    public R<List<KbMcpOptionVo>> options() {
        return R.ok(service.options());
    }

    private List<Long> parseIds(String ids) {
        if (ids == null || ids.isBlank()) {
            return List.of();
        }
        return Arrays.stream(ids.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(Long::valueOf)
            .collect(Collectors.toList());
    }
}
