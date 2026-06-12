package io.github.genkidoudou.web.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.github.genkidoudou.web.ai.dto.AiModelBo;
import io.github.genkidoudou.web.ai.dto.AiModelOptionVo;
import io.github.genkidoudou.web.ai.dto.AiModelQueryBo;
import io.github.genkidoudou.web.ai.dto.AiModelVo;
import io.github.genkidoudou.web.ai.dto.AiSetDefaultBo;
import io.github.genkidoudou.web.ai.dto.AiTestResultVo;
import io.github.genkidoudou.web.ai.service.AiModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
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
 * AI 大模型管理接口。
 */
@Tag(name = "AI 大模型管理")
@Validated
@RestController
@RequestMapping("/ai/model")
@RequiredArgsConstructor
public class AiModelController {

    private final AiModelService service;

    @Operation(summary = "模型分页列表")
    @SaCheckPermission("ai:model:list")
    @GetMapping("/list")
    public R<PageInfo<AiModelVo>> list(@Validated AiModelQueryBo query) {
        return R.ok(service.page(query));
    }

    @Operation(summary = "模型详情")
    @SaCheckPermission("ai:model:query")
    @GetMapping("/getInfo")
    public R<AiModelVo> getInfo(
        @Parameter(description = "模型 ID") @RequestParam @Min(1) Long modelId,
        @Parameter(description = "是否展示密钥明文") @RequestParam(defaultValue = "false") boolean revealSecrets) {
        AiModelVo vo = service.getInfo(modelId, revealSecrets);
        if (vo == null) {
            return R.ok();
        }
        return R.ok(vo);
    }

    @Operation(summary = "新增模型")
    @SaCheckPermission("ai:model:add")
    @PostMapping("/add")
    public R<Void> add(@Validated(AddGroup.class) @RequestBody AiModelBo req) {
        service.add(req);
        return R.ok();
    }

    @Operation(summary = "修改模型")
    @SaCheckPermission("ai:model:edit")
    @PostMapping("/update")
    public R<Void> update(@Validated(UpdateGroup.class) @RequestBody AiModelBo req) {
        service.update(req);
        return R.ok();
    }

    @Operation(summary = "删除模型")
    @SaCheckPermission("ai:model:remove")
    @PostMapping("/remove")
    public R<Void> remove(@RequestBody List<Long> modelIds) {
        service.removeBatch(modelIds);
        return R.ok();
    }

    @Operation(summary = "模型连接测试")
    @SaCheckPermission("ai:model:test")
    @PostMapping("/test")
    public R<AiTestResultVo> test(
        @Parameter(description = "模型 ID") @RequestParam @Min(1) Long modelId) {
        return R.ok(service.test(modelId));
    }

    @Operation(summary = "设为全局默认")
    @SaCheckPermission("ai:model:edit")
    @PostMapping("/setDefault")
    public R<Void> setDefault(@Validated @RequestBody AiSetDefaultBo req) {
        service.setDefault(req);
        return R.ok();
    }

    @Operation(summary = "清除全局默认")
    @SaCheckPermission("ai:model:edit")
    @PostMapping("/clearDefault")
    public R<Void> clearDefault(@RequestBody Map<String, String> body) {
        service.clearDefault(body.get("defaultSlot"));
        return R.ok();
    }

    @Operation(summary = "导出 YAML 或 ENV")
    @SaCheckPermission("ai:model:export")
    @GetMapping("/export")
    public R<String> export(
        @Parameter(description = "逗号分隔的模型 ID") @RequestParam(required = false) String ids,
        @Parameter(description = "格式：yaml 或 env") @RequestParam(defaultValue = "yaml") String format,
        @Parameter(description = "是否包含明文密钥") @RequestParam(defaultValue = "false") boolean includeSecrets) {
        List<Long> modelIds = parseIds(ids);
        return R.ok(service.export(modelIds, format, includeSecrets));
    }

    @Operation(summary = "模型下拉选项")
    @SaCheckPermission("ai:model:list")
    @GetMapping("/options")
    public R<List<AiModelOptionVo>> options(
        @Parameter(description = "模型类型：CHAT / EMBEDDING") @RequestParam(required = false) String modelType) {
        return R.ok(service.options(modelType));
    }

    @Operation(summary = "从 YAML 导入草稿（占位）")
    @SaCheckPermission("ai:model:add")
    @PostMapping("/importFromYaml")
    public R<List<AiModelVo>> importFromYaml() {
        return R.ok(service.importFromYaml());
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
