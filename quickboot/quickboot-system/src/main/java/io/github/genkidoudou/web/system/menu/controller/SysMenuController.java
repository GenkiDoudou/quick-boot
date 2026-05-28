package io.github.genkidoudou.web.system.menu.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.system.menu.domain.SysMenu;
import io.github.genkidoudou.web.system.menu.dto.SysMenuSaveRequest;
import io.github.genkidoudou.web.system.menu.service.MenuService;
import io.github.genkidoudou.web.system.menu.vo.RoleMenuTreeselectVo;
import io.github.genkidoudou.web.system.menu.vo.SysMenuTreeSelectVo;
import io.github.genkidoudou.web.system.menu.vo.SysMenuTreeVo;
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
 * 菜单管理接口。
 */
@Tag(name = "菜单管理")
@Validated
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final MenuService menuService;

    @Operation(summary = "查询菜单树")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/list")
    public R<List<SysMenuTreeVo>> list(
            @Parameter(description = "菜单名称（模糊）") @RequestParam(required = false) String menuName,
            @Parameter(description = "状态：0正常，1停用") @RequestParam(required = false) String status) {
        return R.ok(menuService.listTree(menuName, status));
    }

    @Operation(summary = "菜单下拉树")
    @SaCheckPermission(value = {"system:menu:list", "system:menu:query"}, mode = SaMode.OR)
    @GetMapping("/treeselect")
    public R<List<SysMenuTreeSelectVo>> treeselect(
            @Parameter(description = "为 true 时仅返回目录（M），用于代码生成上级菜单")
            @RequestParam(value = "directoryOnly", defaultValue = "false") boolean directoryOnly,
            @Parameter(description = "为 true 时排除按钮类型（F），仅目录与菜单")
            @RequestParam(value = "excludeButton", defaultValue = "false") boolean excludeButton) {
        if (directoryOnly) {
            return R.ok(menuService.treeselectDirectoryOnly());
        }
        return R.ok(excludeButton ? menuService.treeselectExcludeButton() : menuService.treeselect());
    }

    @Operation(summary = "角色菜单树勾选")
    @SaCheckPermission(value = {"system:role:edit", "system:menu:query"}, mode = SaMode.OR)
    @GetMapping("/roleMenuTreeselect/{roleId:\\d+}")
    public R<RoleMenuTreeselectVo> roleMenuTreeselect(
            @Parameter(description = "角色ID", required = true) @PathVariable @Min(1) Long roleId) {
        return R.ok(menuService.roleMenuTreeselect(roleId));
    }

    @Operation(summary = "菜单详情")
    @SaCheckPermission("system:menu:query")
    @GetMapping("/{menuId:\\d+}")
    public R<SysMenu> getInfo(
            @Parameter(description = "菜单ID", required = true) @PathVariable @Min(1) Long menuId) {
        SysMenu row = menuService.getById(menuId);
        if (row == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "菜单不存在或已删除");
        }
        return R.ok(row);
    }

    @Operation(summary = "新增菜单")
    @SaCheckPermission("system:menu:add")
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysMenuSaveRequest body) {
        menuService.add(body);
        return R.ok();
    }

    @Operation(summary = "修改菜单")
    @SaCheckPermission("system:menu:edit")
    @PostMapping("/update")
    public R<Void> update(@Valid @RequestBody SysMenuSaveRequest body) {
        menuService.update(body);
        return R.ok();
    }

    @Operation(summary = "删除菜单")
    @SaCheckPermission("system:menu:remove")
    @PostMapping("/remove/{menuId:\\d+}")
    public R<Void> remove(
            @Parameter(description = "菜单ID", required = true) @PathVariable @Min(1) Long menuId) {
        menuService.remove(menuId);
        return R.ok();
    }
}
