package io.github.genkidoudou.web.system.dept.controller;

import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.system.dept.domain.SysDept;
import io.github.genkidoudou.web.system.dept.dto.SysDeptSaveRequest;
import io.github.genkidoudou.web.system.dept.service.DeptService;
import io.github.genkidoudou.web.system.dept.vo.SysDeptTreeSelectVo;
import io.github.genkidoudou.web.system.dept.vo.SysDeptTreeVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 部门管理接口。
 */
@Tag(name = "部门管理")
@Validated
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final DeptService deptService;

    @Operation(summary = "查询部门树")
    @GetMapping("/list")
    public R<List<SysDeptTreeVo>> list(
            @Parameter(description = "部门名称") @RequestParam(required = false) String deptName,
            @Parameter(description = "负责人") @RequestParam(required = false) String leader,
            @Parameter(description = "状态：0正常，1停用") @RequestParam(required = false) String status) {
        return R.ok(deptService.listTree(deptName, leader, status));
    }

    @Operation(summary = "查询部门下拉树")
    @GetMapping("/treeselect")
    public R<List<SysDeptTreeSelectVo>> treeselect() {
        return R.ok(deptService.treeselect());
    }

    @Operation(summary = "查询部门详情")
    @GetMapping("/{deptId:\\d+}")
    public R<SysDept> getInfo(
            @Parameter(description = "部门ID", required = true) @PathVariable @Min(value = 1, message = "部门ID必须大于0") Long deptId) {
        SysDept row = deptService.getById(deptId);
        if (row == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "部门不存在或已删除");
        }
        return R.ok(row);
    }

    @Operation(summary = "新增部门")
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysDeptSaveRequest body) {
        deptService.add(body);
        return R.ok();
    }

    @Operation(summary = "修改部门（使用POST，避免PUT）")
    @PostMapping("/update")
    public R<Void> edit(@Valid @RequestBody SysDeptSaveRequest body) {
        deptService.update(body);
        return R.ok();
    }

    @Operation(summary = "删除部门（使用POST，避免DELETE）")
    @PostMapping("/remove/{deptId:\\d+}")
    public R<Void> remove(
            @Parameter(description = "部门ID", required = true) @PathVariable @Min(value = 1, message = "部门ID必须大于0") Long deptId) {
        deptService.remove(deptId);
        return R.ok();
    }
}
