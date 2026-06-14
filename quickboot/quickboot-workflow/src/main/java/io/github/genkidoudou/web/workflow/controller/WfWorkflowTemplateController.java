package io.github.genkidoudou.web.workflow.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.github.genkidoudou.web.workflow.dto.WfSaveTemplateGraphBo;
import io.github.genkidoudou.web.workflow.dto.WfTemplateImportBo;
import io.github.genkidoudou.web.workflow.dto.WfTemplateVo;
import io.github.genkidoudou.web.workflow.dto.WfValidateGraphBo;
import io.github.genkidoudou.web.workflow.dto.WfWorkflowTemplateBo;
import io.github.genkidoudou.web.workflow.dto.WfWorkflowTemplateQueryBo;
import io.github.genkidoudou.web.workflow.dto.WfWorkflowTemplateVo;
import io.github.genkidoudou.web.workflow.service.WorkflowDefinitionService;
import io.github.genkidoudou.web.workflow.service.WorkflowTemplateService;
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

import java.util.List;

/**
 * 工作流模板库管理接口。
 */
@Tag(name = "工作流模板")
@Validated
@RestController
@RequestMapping("/workflow/template")
@RequiredArgsConstructor
public class WfWorkflowTemplateController {

    private final WorkflowTemplateService templateService;
    private final WorkflowDefinitionService definitionService;

    @Operation(summary = "模板分页列表（管理页）")
    @SaCheckPermission("workflow:template:list")
    @GetMapping("/page")
    public R<PageInfo<WfWorkflowTemplateVo>> page(@Validated WfWorkflowTemplateQueryBo query) {
        return R.ok(templateService.page(query));
    }

    @Operation(summary = "模板详情（含 graph）")
    @SaCheckPermission("workflow:template:query")
    @GetMapping("/getInfo")
    public R<WfWorkflowTemplateVo> getInfo(
        @Parameter(description = "模板ID") @RequestParam @Min(1) Long templateId) {
        return R.ok(templateService.getInfo(templateId));
    }

    @Operation(summary = "启用模板列表（新建工作流下拉，不含 graph）")
    @SaCheckPermission("workflow:query")
    @GetMapping("/list")
    public R<List<WfTemplateVo>> list() {
        return R.ok(templateService.listOptions());
    }

    @Operation(summary = "新增模板")
    @SaCheckPermission("workflow:template:add")
    @PostMapping("/add")
    public R<Long> add(@Validated(AddGroup.class) @RequestBody WfWorkflowTemplateBo req) {
        return R.ok(templateService.add(req));
    }

    @Operation(summary = "修改模板")
    @SaCheckPermission("workflow:template:edit")
    @PostMapping("/update")
    public R<Void> update(@Validated(UpdateGroup.class) @RequestBody WfWorkflowTemplateBo req) {
        templateService.update(req);
        return R.ok();
    }

    @Operation(summary = "保存模板图 DSL")
    @SaCheckPermission("workflow:template:edit")
    @PostMapping("/saveGraph")
    public R<Void> saveGraph(@Validated @RequestBody WfSaveTemplateGraphBo req) {
        templateService.saveGraph(req);
        return R.ok();
    }

    @Operation(summary = "校验模板图（不落库）")
    @SaCheckPermission("workflow:template:edit")
    @PostMapping("/validateGraph")
    public R<Void> validateGraph(@Validated @RequestBody WfValidateGraphBo req) {
        definitionService.validateGraph(req.getGraph());
        return R.ok();
    }

    @Operation(summary = "从工作流草稿导入模板")
    @SaCheckPermission("workflow:template:add")
    @PostMapping("/importFromWorkflow")
    public R<Long> importFromWorkflow(@Validated(AddGroup.class) @RequestBody WfTemplateImportBo req) {
        return R.ok(templateService.importFromWorkflow(req));
    }

    @Operation(summary = "删除模板")
    @SaCheckPermission("workflow:template:remove")
    @PostMapping("/remove")
    public R<Void> remove(@RequestBody List<Long> templateIds) {
        templateService.removeBatch(templateIds);
        return R.ok();
    }
}
