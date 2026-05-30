package io.github.genkidoudou.web.system.menu.service;

import io.github.genkidoudou.web.system.menu.domain.SysMenu;
import io.github.genkidoudou.web.system.menu.dto.SysMenuSaveRequest;
import io.github.genkidoudou.web.system.menu.dto.SysMenuSortUpdateRequest;
import io.github.genkidoudou.web.system.menu.vo.RoleMenuTreeselectVo;
import io.github.genkidoudou.web.system.menu.vo.SysMenuTreeSelectVo;
import io.github.genkidoudou.web.system.menu.vo.SysMenuTreeVo;

import java.util.List;
import java.util.Map;

/**
 * 菜单业务：树列表、下拉树、角色菜单树、CRUD、动态路由与权限汇总。
 */
public interface MenuService {

    /**
     * 菜单树列表；无筛选为全量未删树，有筛选时剪枝保留祖先链。
     */
    List<SysMenuTreeVo> listTree(String menuName, String status);

    /**
     * 全量菜单下拉树（不受列表筛选影响）。
     */
    List<SysMenuTreeSelectVo> treeselect();

    /**
     * 代码生成等场景：下拉树仅含目录（M）与菜单（C），不含按钮（F）。
     */
    List<SysMenuTreeSelectVo> treeselectExcludeButton();

    /**
     * 代码生成挂载点：仅含目录（M）。
     */
    List<SysMenuTreeSelectVo> treeselectDirectoryOnly();

    /**
     * 按主键查询未逻辑删除的菜单。
     */
    SysMenu getById(Long menuId);

    void add(SysMenuSaveRequest req);

    void update(SysMenuSaveRequest req);

    /**
     * 批量更新菜单显示排序（列表行内改序号后保存）。
     */
    void updateSort(SysMenuSortUpdateRequest req);

    void remove(Long menuId);

    /**
     * 角色菜单树：全量下拉树 + 该角色已勾选菜单 id。
     */
    RoleMenuTreeselectVo roleMenuTreeselect(Long roleId);

    /**
     * 构建当前用户可见的动态路由（若依 JSON 形状），供 {@code /getRouters} 使用。
     */
    List<Map<String, Object>> buildRouterVos(Long userId);

    /**
     * 用户拥有的角色标识（{@code role_key} 列表）。
     */
    List<String> listRoleKeysByUserId(Long userId);

    /**
     * 用户权限标识集合（含菜单按钮 perms）；超级管理员角色额外包含 {@code *:*:*}。
     */
    List<String> listPermissionsByUserId(Long userId);
}
