package io.github.genkidoudou.web.system.menu.controller;

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
    @GetMapping("/list")
    public R<List<SysMenuTreeVo>> list(
            @Parameter(description = "菜单名称（模糊）") @RequestParam(required = false) String menuName,
            @Parameter(description = "状态：0正常，1停用") @RequestParam(required = false) String status) {
        return R.ok(menuService.listTree(menuName, status));
    }

    @Operation(summary = "菜单下拉树")
    @GetMapping("/treeselect")
    public R<List<SysMenuTreeSelectVo>> treeselect() {
        return R.ok(menuService.treeselect());
    }

    @Operation(summary = "角色菜单树勾选")
    @GetMapping("/roleMenuTreeselect/{roleId:\\d+}")
    public R<RoleMenuTreeselectVo> roleMenuTreeselect(
            @Parameter(description = "角色ID", required = true) @PathVariable @Min(1) Long roleId) {
        return R.ok(menuService.roleMenuTreeselect(roleId));
    }

    @Operation(summary = "菜单详情")
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
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysMenuSaveRequest body) {
        menuService.add(body);
        return R.ok();
    }

    @Operation(summary = "修改菜单")
    @PostMapping("/update")
    public R<Void> update(@Valid @RequestBody SysMenuSaveRequest body) {
        menuService.update(body);
        return R.ok();
    }

    @Operation(summary = "删除菜单")
    @PostMapping("/remove/{menuId:\\d+}")
    public R<Void> remove(
            @Parameter(description = "菜单ID", required = true) @PathVariable @Min(1) Long menuId) {
        menuService.remove(menuId);
        return R.ok();
    }
}
