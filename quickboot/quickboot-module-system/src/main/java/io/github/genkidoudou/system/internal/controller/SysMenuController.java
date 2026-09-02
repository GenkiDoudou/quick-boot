package io.github.genkidoudou.system.internal.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.idempotency.Idempotent;
import io.github.genkidoudou.common.web.DeprecatedApiSupport;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.common.security.utils.LoginUserUtils;
import io.github.genkidoudou.common.security.vo.LoginUser;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.github.genkidoudou.system.internal.service.ISysH5HomeShortcutService;
import io.github.genkidoudou.system.internal.service.ISysMenuService;
import io.github.genkidoudou.system.internal.service.ISysPermissionService;
import io.github.genkidoudou.system.internal.vo.H5HomeShortcutSaveVo;
import io.github.genkidoudou.system.internal.vo.H5WorkbenchGroupVo;
import io.github.genkidoudou.system.internal.vo.H5WorkbenchItemVo;
import io.github.genkidoudou.system.internal.vo.MenuSortVo;
import io.github.genkidoudou.system.internal.vo.MenuTreeSelectVo;
import io.github.genkidoudou.system.internal.vo.SysMenuImportRow;
import io.github.genkidoudou.system.internal.vo.SysMenuVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "菜单管理")
@RequiredArgsConstructor
@RestController
@RequestMapping("system/menu")
public class SysMenuController {

  private final ISysMenuService menuService;

  private final ISysPermissionService permissionService;

  private final ISysH5HomeShortcutService h5HomeShortcutService;

  /**
   * quick-h5 工作台菜单：按当前用户角色返回分组与 /pages/ 入口。
   * <p>仅需登录；不要求 system:menu:list。约定：C 节点 path 以 /pages/ 开头。</p>
   *
   * @return 工作台分组列表
   */
  @Operation(summary = "H5 工作台菜单")
  @GetMapping("/h5Workbench")
  public R<List<H5WorkbenchGroupVo>> h5Workbench() {
    StpUtil.checkLogin();
    String userId = currentUserId();
    if (StrUtil.isBlank(userId)) {
      return R.ok(List.of());
    }
    return R.ok(permissionService.buildH5Workbench(userId));
  }

  /**
   * H5 首页最终快捷宫格（偏好或默认 ∩ 授权，最多 8）。
   *
   * @return 扁平入口列表
   */
  @Operation(summary = "H5 首页快捷")
  @GetMapping("/h5HomeShortcuts")
  public R<List<H5WorkbenchItemVo>> h5HomeShortcuts() {
    StpUtil.checkLogin();
    String userId = currentUserId();
    if (StrUtil.isBlank(userId)) {
      return R.ok(List.of());
    }
    return R.ok(h5HomeShortcutService.listFinalShortcuts(userId));
  }

  /**
   * H5 首页快捷候选池（与工作台叶子同源）。
   *
   * @return 扁平入口列表
   */
  @Operation(summary = "H5 首页快捷候选")
  @GetMapping("/h5HomeShortcutCandidates")
  public R<List<H5WorkbenchItemVo>> h5HomeShortcutCandidates() {
    StpUtil.checkLogin();
    String userId = currentUserId();
    if (StrUtil.isBlank(userId)) {
      return R.ok(List.of());
    }
    return R.ok(h5HomeShortcutService.listCandidates(userId));
  }

  /**
   * 保存 H5 首页快捷偏好（全量覆盖）。
   * <p>{@code menuIds} 为空则清除偏好并恢复默认；禁止 PUT/DELETE。</p>
   *
   * @param body menuIds
   * @return ok
   */
  @Operation(summary = "保存 H5 首页快捷")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':h5sc:' + #body.menuIds", message = "请勿重复提交")
  @PostMapping("/h5HomeShortcuts/save")
  public R<Void> saveH5HomeShortcuts(@RequestBody(required = false) H5HomeShortcutSaveVo body) {
    StpUtil.checkLogin();
    String userId = currentUserId();
    if (StrUtil.isBlank(userId)) {
      return R.ok();
    }
    List<String> menuIds = body == null || body.getMenuIds() == null
      ? List.of()
      : body.getMenuIds();
    h5HomeShortcutService.saveShortcuts(userId, menuIds);
    return R.ok();
  }

  /** 当前登录用户 id 字符串；未登录或无主体时返回 null。 */
  private static String currentUserId() {
    LoginUser loginUser = LoginUserUtils.getLoginUser();
    return loginUser == null || loginUser.getUserId() == null
      ? null
      : String.valueOf(loginUser.getUserId());
  }

  /**
   * 菜单树列表（POST；records 为根节点树，非物理分页）。
   *
   * @param pageRequest 筛选条件放在 param（menuName/status）
   * @return 树形列表包装为 PageInfo
   */
  @Operation(summary = "菜单树形列表")
  @SaCheckPermission("system:menu:list")
  @PostMapping("/page")
  public R<PageInfo<SysMenuVo>> page(@RequestBody(required = false) PageRequest<SysMenuVo> pageRequest) {
    SysMenuVo param = pageRequest != null ? pageRequest.getParam() : null;
    String menuName = param != null ? param.getMenuName() : null;
    String status = param != null ? param.getStatus() : null;
    List<SysMenuVo> tree = menuService.listTree(menuName, status);
    PageInfo<SysMenuVo> info = new PageInfo<>();
    info.setCurrent(1);
    info.setSize(tree.size());
    info.setRecords(tree);
    info.setTotal(tree.size());
    info.setPages(1);
    return R.ok(info);
  }

  /**
   * 菜单树列表（GET 兼容，请改用 POST {@code /page}）。
   *
   * @param menuName 名称模糊
   * @param status   状态
   * @return 树
   * @deprecated 请改用 POST {@code /system/menu/page}，data 取 records
   */
  @Deprecated
  @Operation(summary = "菜单树列表（兼容）", deprecated = true)
  @SaCheckPermission("system:menu:list")
  @GetMapping("/list")
  public R<List<SysMenuVo>> list(
    HttpServletResponse response,
    @RequestParam(required = false) String menuName,
    @RequestParam(required = false) String status) {
    DeprecatedApiSupport.markDeprecated(response);
    return R.ok(menuService.listTree(menuName, status));
  }

  /**
   * 菜单下拉树。
   *
   * @param excludeButton 排除按钮
   * @param directoryOnly 仅目录
   * @return 树
   */
  @Operation(summary = "菜单下拉树")
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
  @Operation(summary = "角色菜单树")
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
  @Operation(summary = "菜单详情")
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
  @Operation(summary = "新增菜单")
  @SaCheckPermission("system:menu:add")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':add:' + #body.menuName + ':' + #body.parentId", message = "请勿重复提交")
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
  @Operation(summary = "修改菜单")
  @SaCheckPermission("system:menu:edit")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':upd:' + #body.menuId", message = "请勿重复提交")
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
  @Operation(summary = "批量保存排序")
  @SaCheckPermission("system:menu:edit")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':sort:' + #sort.menuIds", message = "请勿重复提交")
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
  @Operation(summary = "删除菜单")
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
  @Operation(summary = "批量删除菜单")
  @SaCheckPermission("system:menu:remove")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':rm:' + #menuIds", message = "请勿重复提交")
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
  @Operation(summary = "导出菜单")
  @SaCheckPermission("system:menu:export")
  @PostMapping("/export")
  public void export(@RequestBody(required = false) SysMenuVo request,
                     HttpServletResponse response) throws Exception {
    List<SysMenuVo> export = menuService.export(request);
    ExcelUtils.exportExcel(export, "菜单", SysMenuVo.class, response);
  }

}
