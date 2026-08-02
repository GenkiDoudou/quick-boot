package io.github.genkidoudou.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.github.genkidoudou.system.service.ISysMenuService;
import io.github.genkidoudou.system.vo.MenuSortVo;
import io.github.genkidoudou.system.vo.MenuTreeSelectVo;
import io.github.genkidoudou.system.vo.SysMenuImportRow;
import io.github.genkidoudou.system.vo.SysMenuVo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 系统菜单管理。CRUD 契约对齐 {@link SysOauthClientController} / {@link SysRoleController}。
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("system/menu")
public class SysMenuController {

  private final ISysMenuService menuService;

  /**
   * 菜单树列表。
   *
   * @param menuName 名称模糊
   * @param status   状态
   * @return 树
   */
  @SaCheckPermission("system:menu:list")
  @GetMapping("/list")
  public R<List<SysMenuVo>> list(
    @RequestParam(required = false) String menuName,
    @RequestParam(required = false) String status) {
    return R.ok(menuService.listTree(menuName, status));
  }

  /**
   * 菜单下拉树。
   *
   * @param excludeButton 排除按钮
   * @param directoryOnly 仅目录
   * @return 树
   */
  @SaCheckPermission(value = {"system:menu:query", "system:menu:list", "system:menu:add", "system:menu:edit"}, mode = SaMode.OR)
  @GetMapping("/treeselect")
  public R<List<MenuTreeSelectVo>> treeselect(
    @RequestParam(required = false, defaultValue = "false") boolean excludeButton,
    @RequestParam(required = false, defaultValue = "false") boolean directoryOnly) {
    return R.ok(menuService.treeselect(excludeButton, directoryOnly));
  }

  /**
   * 角色菜单树（含已勾选 keys）。
   *
   * @param roleId 角色 id
   * @return menus + checkedKeys
   */
  @SaCheckPermission(value = {"system:role:menu", "system:menu:query"}, mode = SaMode.OR)
  @GetMapping("/roleMenuTreeselect/{roleId}")
  public R<Map<String, Object>> roleMenuTreeselect(@PathVariable Long roleId) {
    return R.ok(menuService.roleMenuTreeselect(roleId));
  }

  /**
   * 菜单详情。
   *
   * @param menuId 主键
   * @return Vo
   */
  @SaCheckPermission(value = {"system:menu:query", "system:menu:list"}, mode = SaMode.OR)
  @GetMapping("/{menuId}")
  public R<SysMenuVo> get(@PathVariable Long menuId) {
    return R.ok(menuService.getDetail(menuId));
  }

  /**
   * 新增菜单；data 为新建 menuId。
   *
   * @param vo 可写字段
   * @return 主键
   */
  @SaCheckPermission("system:menu:add")
  @PostMapping("/add")
  public R<String> add(@RequestBody @Validated(AddGroup.class) SysMenuVo vo) {
    Long id = menuService.add(vo);
    return R.ok(id == null ? null : String.valueOf(id));
  }

  /**
   * 修改菜单。
   *
   * @param vo 含 menuId
   * @return 是否成功
   */
  @SaCheckPermission("system:menu:edit")
  @PostMapping("/update")
  public R<Boolean> update(@RequestBody @Validated(UpdateGroup.class) SysMenuVo vo) {
    return R.ok(menuService.update(vo));
  }

  /**
   * 批量保存排序。
   *
   * @param sort menuIds 与 orderNums
   * @return ok
   */
  @SaCheckPermission("system:menu:edit")
  @PostMapping("/updateSort")
  public R<Void> updateSort(@RequestBody MenuSortVo sort) {
    menuService.updateSort(sort);
    return R.ok();
  }

  /**
   * 单条删除。
   *
   * @param menuId 主键
   * @return ok
   */
  @SaCheckPermission("system:menu:remove")
  @GetMapping("/remove/{menuId}")
  public R<Void> removeGet(@PathVariable Long menuId) {
    menuService.remove(List.of(menuId));
    return R.ok();
  }

  /**
   * 批量删除。
   *
   * @param menuIds 主键集合
   * @return ok
   */
  @SaCheckPermission("system:menu:remove")
  @PostMapping("/remove")
  public R<Void> remove(@RequestBody List<Long> menuIds) {
    menuService.remove(menuIds);
    return R.ok();
  }

  /**
   * 同步导出 xlsx（扁平行）。有 ids 则按勾选；否则按搜索条件。
   *
   * @param request  导出条件
   * @param response 文件流
   */
  @SaCheckPermission("system:menu:export")
  @PostMapping("/export")
  public void export(@RequestBody(required = false) SysMenuVo request,
                     HttpServletResponse response) throws Exception {
    List<SysMenuVo> export = menuService.export(request);
    ExcelUtils.exportExcel(export, "菜单", SysMenuVo.class, response);
  }

}
