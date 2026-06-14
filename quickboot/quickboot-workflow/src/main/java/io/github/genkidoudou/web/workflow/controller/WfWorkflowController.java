package io.github.genkidoudou.web.workflow.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.github.genkidoudou.web.workflow.dto.WfPublishBo;
import io.github.genkidoudou.web.workflow.dto.WfSaveGraphBo;
import io.github.genkidoudou.web.workflow.dto.WfValidateGraphBo;
import io.github.genkidoudou.web.workflow.dto.WfWorkflowBo;
import io.github.genkidoudou.web.workflow.dto.WfWorkflowDetailVo;
import io.github.genkidoudou.web.workflow.dto.WfWorkflowQueryBo;
import io.github.genkidoudou.web.workflow.dto.WfWorkflowVo;
import io.github.genkidoudou.web.workflow.service.WorkflowDefinitionService;
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
 * 工作流定义管理接口。
 */
@Tag(name = "工作流管理")
@Validated
@RestController
@RequestMapping("/workflow")
@RequiredArgsConstructor
public class WfWorkflowController {

    private final WorkflowDefinitionService definitionService;

    @Operation(summary = "工作流分页列表")
    @SaCheckPermission("workflow:list")
    @GetMapping("/list")
    public R<PageInfo<WfWorkflowVo>> list(@Validated WfWorkflowQueryBo query) {
        return R.ok(definitionService.page(query));
    }

    @Operation(summary = "工作流详情（含草稿 graph）")
    @SaCheckPermission("workflow:query")
    @GetMapping("/getInfo")
    public R<WfWorkflowDetailVo> getInfo(
        @Parameter(description = "工作流ID") @RequestParam @Min(1) Long workflowId) {
        return R.ok(definitionService.getDetail(workflowId));
    }

    @Operation(summary = "新增工作流")
    @SaCheckPermission("workflow:add")
    @PostMapping("/add")
    public R<Void> add(@Validated(AddGroup.class) @RequestBody WfWorkflowBo req) {
        definitionService.add(req);
        return R.ok();
    }

    @Operation(summary = "修改工作流元数据")
    @SaCheckPermission("workflow:edit")
    @PostMapping("/update")
    public R<Void> update(@Validated(UpdateGroup.class) @RequestBody WfWorkflowBo req) {
        definitionService.update(req);
        return R.ok();
    }

    @Operation(summary = "保存工作流图 DSL（草稿）")
    @SaCheckPermission("workflow:edit")
    @PostMapping("/saveGraph")
    public R<Void> saveGraph(@Validated @RequestBody WfSaveGraphBo req) {
        definitionService.saveGraph(req);
        return R.ok();
    }

    @Operation(summary = "校验工作流图（不落库）")
    @SaCheckPermission("workflow:edit")
    @PostMapping("/validateGraph")
    public R<Void> validateGraph(@Validated @RequestBody WfValidateGraphBo req) {
        definitionService.validateGraph(req.getGraph());
        return R.ok();
    }

    @Operation(summary = "发布工作流")
    @SaCheckPermission("workflow:publish")
    @PostMapping("/publish")
    public R<Void> publish(@Validated @RequestBody WfPublishBo req) {
        definitionService.publish(req);
        return R.ok();
    }

    @Operation(summary = "删除工作流")
    @SaCheckPermission("workflow:remove")
    @PostMapping("/remove")
    public R<Void> remove(@RequestBody List<Long> workflowIds) {
        definitionService.removeBatch(workflowIds);
        return R.ok();
    }

}
