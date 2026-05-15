package io.github.genkidoudou.web.system.role.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.github.genkidoudou.web.system.role.dto.RoleCancelUserRequest;
import io.github.genkidoudou.web.system.role.dto.RoleChangeStatusRequest;
import io.github.genkidoudou.web.system.role.dto.RoleDataScopeRequest;
import io.github.genkidoudou.web.system.role.dto.RoleGrantUsersRequest;
import io.github.genkidoudou.web.system.role.dto.RoleMenuRequest;
import io.github.genkidoudou.web.system.role.dto.SysRoleAuthUserQueryBo;
import io.github.genkidoudou.web.system.role.dto.SysRoleBo;
import io.github.genkidoudou.web.system.role.dto.SysRoleQueryBo;
import io.github.genkidoudou.web.system.role.dto.SysRoleUserVo;
import io.github.genkidoudou.web.system.role.dto.SysRoleVo;
import io.github.genkidoudou.web.system.role.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色管理接口。
 */
@Tag(name = "角色管理")
@Validated
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService service;

    @Operation(summary = "角色分页列表")
    @SaCheckPermission("system:role:list")
    @GetMapping("/list")
    public R<PageInfo<SysRoleVo>> list(@Validated SysRoleQueryBo query) {
        return R.ok(service.page(query));
    }

    @Operation(summary = "角色详情")
    @SaCheckPermission("system:role:list")
    @GetMapping("/{roleId}")
    public R<SysRoleVo> get(@Parameter(description = "角色ID") @PathVariable @Min(1) Long roleId) {
        SysRoleVo vo = service.getById(roleId);
        if (vo == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "角色不存在或已删除");
        }
        return R.ok(vo);
    }

    @Operation(summary = "新增角色")
    @SaCheckPermission("system:role:add")
    @PostMapping("/create")
    public R<Void> create(@Validated(AddGroup.class) @RequestBody SysRoleBo req) {
        service.add(req);
        return R.ok();
    }

    @Operation(summary = "修改角色")
    @SaCheckPermission("system:role:edit")
    @PostMapping("/update")
    public R<Void> update(@Validated(UpdateGroup.class) @RequestBody SysRoleBo req) {
        service.update(req);
        return R.ok();
    }

    @Operation(summary = "删除角色（支持批量）")
    @SaCheckPermission("system:role:remove")
    @PostMapping("/remove")
    public R<Void> remove(@RequestBody List<Long> roleIds) {
        service.removeBatch(roleIds);
        return R.ok();
    }

    @Operation(summary = "修改角色状态")
    @SaCheckPermission("system:role:edit")
    @PostMapping("/changeStatus")
    public R<Void> changeStatus(@Validated @RequestBody RoleChangeStatusRequest req) {
        service.changeStatus(req);
        return R.ok();
    }

    @Operation(summary = "保存数据权限")
    @SaCheckPermission("system:role:dataScope")
    @PostMapping("/dataScope")
    public R<Void> dataScope(@Validated @RequestBody RoleDataScopeRequest req) {
        service.updateDataScope(req);
        return R.ok();
    }

    @Operation(summary = "保存角色菜单")
    @SaCheckPermission("system:role:edit")
    @PostMapping("/menu")
    public R<Void> menu(@Validated @RequestBody RoleMenuRequest req) {
        service.updateMenus(req);
        return R.ok();
    }

    @Operation(summary = "已分配用户分页")
    @SaCheckPermission("system:role:list")
    @GetMapping("/authUser/allocatedList")
    public R<PageInfo<SysRoleUserVo>> allocatedList(@Validated SysRoleAuthUserQueryBo query) {
        return R.ok(service.pageAllocatedUsers(query));
    }

    @Operation(summary = "未分配用户分页")
    @SaCheckPermission("system:role:list")
    @GetMapping("/authUser/unallocatedList")
    public R<PageInfo<SysRoleUserVo>> unallocatedList(@Validated SysRoleAuthUserQueryBo query) {
        return R.ok(service.pageUnallocatedUsers(query));
    }

    @Operation(summary = "批量授权用户")
    @SaCheckPermission("system:role:edit")
    @PostMapping("/authUser/selectAll")
    public R<Void> selectAll(@Validated @RequestBody RoleGrantUsersRequest req) {
        service.grantUsers(req);
        return R.ok();
    }

    @Operation(summary = "取消单个用户授权")
    @SaCheckPermission("system:role:edit")
    @PostMapping("/authUser/cancel")
    public R<Void> cancel(@Validated @RequestBody RoleCancelUserRequest req) {
        service.cancelUser(req);
        return R.ok();
    }

    @Operation(summary = "批量取消用户授权")
    @SaCheckPermission("system:role:edit")
    @PostMapping("/authUser/cancelAll")
    public R<Void> cancelAll(@Validated @RequestBody RoleGrantUsersRequest req) {
        service.cancelUsers(req);
        return R.ok();
    }

    @Operation(summary = "导出角色")
    @SaCheckPermission("system:role:export")
    @PostMapping("/export")
    public void export(SysRoleQueryBo query, HttpServletResponse response) {
        service.export(query, response);
    }
}
