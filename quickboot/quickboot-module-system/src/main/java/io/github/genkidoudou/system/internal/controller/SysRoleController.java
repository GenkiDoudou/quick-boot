package io.github.genkidoudou.system.internal.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.idempotency.Idempotent;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.github.genkidoudou.system.internal.service.ISysRoleService;
import io.github.genkidoudou.system.internal.vo.RoleMenuTreeVo;
import io.github.genkidoudou.system.internal.vo.SysRoleImportRow;
import io.github.genkidoudou.system.internal.vo.SysRoleUserVo;
import io.github.genkidoudou.system.internal.vo.SysRoleVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
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

/**
 * 角色管理。CRUD 契约对齐 {@link SysOauthClientController}；菜单/用户授权为扩展能力。
 */
@Tag(name = "角色管理")
@RequiredArgsConstructor
@RestController
@RequestMapping("sys/role")
public class SysRoleController {

  private final ISysRoleService roleService;

  /**
   * 角色分页。
   *
   * @param pageRequest 分页参数
   * @return 分页结果
   */
  @Operation(summary = "分页查询")
  @SaCheckPermission("system:role:list")
  @PostMapping("page")
  public R<PageInfo<SysRoleVo>> page(@RequestBody PageRequest<SysRoleVo> pageRequest) {
    return R.ok(roleService.page(pageRequest));
  }

  /**
   * 角色详情。
   *
   * @param roleId 主键
   * @return Vo
   */
  @Operation(summary = "角色详情")
  @SaCheckPermission(value = {"system:role:list", "system:role:query", "system:role:edit"}, mode = SaMode.OR)
  @GetMapping("/{roleId}")
  public R<SysRoleVo> get(@PathVariable Long roleId) {
    return R.ok(roleService.getDetail(roleId));
  }

  /**
   * 新增角色；响应 data 为新建主键 roleId。
   *
   * @param vo 可写字段
   * @return 主键
   */
  @Operation(summary = "新增角色")
  @SaCheckPermission("system:role:add")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':add:' + #body.roleKey", message = "请勿重复提交")
  @PostMapping("add")
  public R<String> add(@RequestBody @Validated(AddGroup.class) SysRoleVo vo) {
    Long roleId = roleService.add(vo);
    return R.ok(roleId == null ? null : String.valueOf(roleId));
  }

  /**
   * 修改角色。
   *
   * @param vo 含 roleId
   * @return 是否成功
   */
  @Operation(summary = "修改角色")
  @SaCheckPermission("system:role:edit")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':upd:' + #body.roleId", message = "请勿重复提交")
  @PostMapping("update")
  public R<Boolean> update(@RequestBody @Validated(UpdateGroup.class) SysRoleVo vo) {
    return R.ok(roleService.update(vo));
  }

  /**
   * 单条删除。
   *
   * @param roleId 主键
   * @return ok
   */
  @Operation(summary = "删除角色")
  @SaCheckPermission("system:role:remove")
  @GetMapping("remove/{roleId}")
  public R<Void> removeGet(@PathVariable Long roleId) {
    roleService.remove(List.of(roleId));
    return R.ok();
  }

  /**
   * 批量删除。
   *
   * @param roleIds 主键集合
   * @return ok
   */
  @Operation(summary = "批量删除角色")
  @SaCheckPermission("system:role:remove")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':rm:' + #roleIds", message = "请勿重复提交")
  @PostMapping("/remove")
  public R<Void> remove(@RequestBody List<Long> roleIds) {
    roleService.remove(roleIds);
    return R.ok();
  }

  /**
   * 修改状态。
   *
   * @param body roleId + status
   * @return ok
   */
  @Operation(summary = "修改角色状态")
  @SaCheckPermission("system:role:edit")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':st:' + #body.roleId + ':' + #body.status", message = "请勿重复提交")
  @PostMapping("changeStatus")
  public R<Void> changeStatus(@RequestBody ChangeStatusBody body) {
    roleService.changeStatus(body.getRoleId(), body.getStatus());
    return R.ok();
  }

  /**
   * 全量保存角色菜单。
   *
   * @param body roleId + menuIds
   * @return ok
   */
  @Operation(summary = "保存角色菜单")
  @SaCheckPermission("system:role:menu")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':menu:' + #body.roleId + ':' + #body.menuIds", message = "请勿重复提交")
  @PostMapping("menu")
  public R<Void> saveMenu(@RequestBody RoleMenuBody body) {
    roleService.saveMenus(body.getRoleId(), body.getMenuIds());
    return R.ok();
  }

  /**
   * 角色菜单树。
   *
   * @param roleId 主键
   * @return 树与勾选
   */
  @Operation(summary = "角色菜单树")
  @SaCheckPermission("system:role:menu")
  @GetMapping("menuTree")
  public R<RoleMenuTreeVo> menuTree(@RequestParam Long roleId) {
    return R.ok(roleService.menuTree(roleId));
  }

  /**
   * 已分配用户分页。
   *
   * @param body 分页体
   * @return 分页
   */
  @Operation(summary = "已分配用户分页")
  @SaCheckPermission("system:role:authUser")
  @PostMapping("authUser/allocatedPage")
  public R<PageInfo<SysRoleUserVo>> allocatedPage(@RequestBody AuthUserPageBody body) {
    PageRequest<SysRoleUserVo> req = body != null ? body.toPageRequest() : new PageRequest<>();
    return R.ok(roleService.allocatedPage(req, body != null ? body.getRoleId() : null));
  }

  /**
   * 未分配用户分页。
   *
   * @param body 分页体
   * @return 分页
   */
  @Operation(summary = "未分配用户分页")
  @SaCheckPermission("system:role:authUser")
  @PostMapping("authUser/unallocatedPage")
  public R<PageInfo<SysRoleUserVo>> unallocatedPage(@RequestBody AuthUserPageBody body) {
    PageRequest<SysRoleUserVo> req = body != null ? body.toPageRequest() : new PageRequest<>();
    return R.ok(roleService.unallocatedPage(req, body != null ? body.getRoleId() : null));
  }

  /**
   * 授权用户。
   *
   * @param body roleId + userIds
   * @return ok
   */
  @Operation(summary = "授权用户")
  @SaCheckPermission("system:role:authUser")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':grant:' + #body.roleId + ':' + #body.userIds", message = "请勿重复提交")
  @PostMapping("authUser/grant")
  public R<Void> grant(@RequestBody RoleUsersBody body) {
    roleService.grantUsers(body.getRoleId(), body.getUserIds());
    return R.ok();
  }

  /**
   * 取消用户授权。
   *
   * @param body roleId + userIds
   * @return ok
   */
  @Operation(summary = "取消用户授权")
  @SaCheckPermission("system:role:authUser")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':cancel:' + #body.roleId + ':' + #body.userIds", message = "请勿重复提交")
  @PostMapping("authUser/cancel")
  public R<Void> cancel(@RequestBody RoleUsersBody body) {
    roleService.cancelUsers(body.getRoleId(), body.getUserIds());
    return R.ok();
  }

  /**
   * 批量取消用户授权。
   *
   * @param body roleId + userIds
   * @return ok
   */
  @Operation(summary = "批量取消用户授权")
  @SaCheckPermission("system:role:authUser")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':cancelAll:' + #body.roleId + ':' + #body.userIds", message = "请勿重复提交")
  @PostMapping("authUser/cancelAll")
  public R<Void> cancelAll(@RequestBody RoleUsersBody body) {
    roleService.cancelUsers(body.getRoleId(), body.getUserIds());
    return R.ok();
  }

  /**
   * 同步导出 xlsx。有 ids 则按勾选；否则按搜索条件。
   */
  @Operation(summary = "导出角色")
  @SaCheckPermission("system:role:export")
  @PostMapping("/export")
  public void export(@RequestBody(required = false) SysRoleVo request,
                     HttpServletResponse response) throws Exception {
    List<SysRoleVo> export = roleService.export(request);
    ExcelUtils.exportExcel(export, "角色", SysRoleVo.class, response);
  }

  /**
   * 导入模板。
   */
  @Operation(summary = "导入模板")
  @SaCheckPermission("system:role:import")
  @GetMapping("/import/template")
  public void importTemplate(HttpServletResponse response) {
    ExcelUtils.exportExcel(Collections.emptyList(), "role-import-template",
      SysRoleImportRow.class, false, true, response);
  }

  /**
   * 同步导入；可选更新已存在 roleKey。
   */
  @Operation(summary = "导入角色")
  @SaCheckPermission("system:role:import")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':import:role'", message = "请勿重复提交")
  @PostMapping("/import")
  public R<ExcelResult<SysRoleImportRow>> importExcel(@RequestParam("file") MultipartFile file,
                                                      @RequestParam(value = "updateSupport", defaultValue = "false")
                                                      String updateSupport) throws IOException {
    boolean update = "true".equalsIgnoreCase(updateSupport) || "1".equals(updateSupport);
    return R.ok(roleService.importExcel(file, update));
  }

  @Data
  public static class ChangeStatusBody {
    private Long roleId;
    private String status;
  }

  @Data
  public static class RoleMenuBody {
    private Long roleId;
    private List<Long> menuIds;
  }

  @Data
  public static class RoleUsersBody {
    private Long roleId;
    private List<String> userIds;
  }

  @Data
  public static class AuthUserPageBody {
    private Long roleId;
    private int current = 1;
    private int size = 10;
    private SysRoleUserVo param;

    PageRequest<SysRoleUserVo> toPageRequest() {
      return new PageRequest<>(current, size, param);
    }
  }
}
