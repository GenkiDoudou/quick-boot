package io.github.genkidoudou.system.internal.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.desensitization.SensitiveResponse;
import io.github.genkidoudou.common.idempotency.Idempotent;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.github.genkidoudou.system.internal.service.ISysUserService;
import io.github.genkidoudou.system.internal.vo.SysUserAuthRoleVo;
import io.github.genkidoudou.system.internal.vo.SysUserImportRow;
import io.github.genkidoudou.system.internal.vo.SysUserVo;
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
 * 系统用户管理。CRUD、状态/密码、角色授权及 Excel 导入导出。
 */
@Tag(name = "用户管理")
@RequiredArgsConstructor
@RestController
@RequestMapping("sys/user")
public class SysUserController {

  private final ISysUserService userService;

  /**
   * 用户分页。
   *
   * @param pageRequest 分页参数与查询条件
   * @return 分页结果（敏感字段已脱敏）
   */
  @Operation(summary = "分页查询")
  @SaCheckPermission("system:user:list")
  @SensitiveResponse
  @PostMapping("page")
  public R<PageInfo<SysUserVo>> page(@RequestBody PageRequest<SysUserVo> pageRequest) {
    return R.ok(userService.page(pageRequest));
  }

  /**
   * 用户详情。
   *
   * @param userId 用户主键
   * @return 用户 Vo
   */
  @Operation(summary = "用户详情")
  @SaCheckPermission(value = {"system:user:query", "system:user:list", "system:user:edit"}, mode = SaMode.OR)
  @GetMapping("/{userId}")
  public R<SysUserVo> get(@PathVariable Long userId) {
    return R.ok(userService.getDetail(userId));
  }

  /**
   * 新增用户；响应 data 为新建 userId。
   *
   * @param vo 可写字段（含初始密码）
   * @return 新建主键
   */
  @Operation(summary = "新增用户")
  @SaCheckPermission("system:user:add")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':add:' + #body.userName", message = "请勿重复提交")
  @PostMapping("add")
  public R<String> add(@RequestBody @Validated(AddGroup.class) SysUserVo vo) {
    Long id = userService.add(vo);
    return R.ok(id == null ? null : String.valueOf(id));
  }

  /**
   * 修改用户。
   *
   * @param vo 含 userId 的可写字段
   * @return 是否成功
   */
  @Operation(summary = "修改用户")
  @SaCheckPermission("system:user:edit")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':upd:' + #body.userId", message = "请勿重复提交")
  @PostMapping("update")
  public R<Boolean> update(@RequestBody @Validated(UpdateGroup.class) SysUserVo vo) {
    return R.ok(userService.update(vo));
  }

  /**
   * 单条删除用户。
   *
   * @param userId 用户主键
   * @return ok
   */
  @Operation(summary = "删除用户")
  @SaCheckPermission("system:user:remove")
  @GetMapping("remove/{userId}")
  public R<Void> removeGet(@PathVariable Long userId) {
    userService.remove(List.of(userId));
    return R.ok();
  }

  /**
   * 批量删除用户。
   *
   * @param userIds 用户主键集合
   * @return ok
   */
  @Operation(summary = "批量删除用户")
  @SaCheckPermission("system:user:remove")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':rm:' + #userIds", message = "请勿重复提交")
  @PostMapping("remove")
  public R<Void> remove(@RequestBody List<Long> userIds) {
    userService.remove(userIds);
    return R.ok();
  }

  /**
   * 修改用户启用/停用状态。
   *
   * @param body userId + status
   * @return ok
   */
  @Operation(summary = "修改用户状态")
  @SaCheckPermission("system:user:edit")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':st:' + #body.userId + ':' + #body.status", message = "请勿重复提交")
  @PostMapping("changeStatus")
  public R<Void> changeStatus(@RequestBody ChangeStatusBody body) {
    userService.changeStatus(body.getUserId(), body.getStatus());
    return R.ok();
  }

  /**
   * 重置用户登录密码。
   *
   * @param body userId + 新密码明文
   * @return ok；副作用为更新密码哈希
   */
  @Operation(summary = "重置密码")
  @SaCheckPermission("system:user:resetPwd")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':pwd:' + #body.userId", message = "请勿重复提交")
  @PostMapping("resetPwd")
  public R<Void> resetPwd(@RequestBody ResetPwdBody body) {
    userService.resetPwd(body.getUserId(), body.getPassword());
    return R.ok();
  }

  /**
   * 查询用户已授权角色（编辑页回显）。
   *
   * @param userId 用户主键
   * @return 用户信息与角色勾选列表
   */
  @Operation(summary = "查询授权角色")
  @SaCheckPermission("system:user:edit")
  @GetMapping("authRole/{userId}")
  public R<SysUserAuthRoleVo> authRole(@PathVariable Long userId) {
    return R.ok(userService.authRole(userId));
  }

  /**
   * 全量保存用户角色授权。
   *
   * @param body userId + roleIds
   * @return ok
   */
  @Operation(summary = "保存授权角色")
  @SaCheckPermission("system:user:edit")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':role:' + #body.userId + ':' + #body.roleIds", message = "请勿重复提交")
  @PostMapping("authRole")
  public R<Void> saveAuthRole(@RequestBody AuthRoleBody body) {
    userService.saveAuthRole(body.getUserId(), body.getRoleIds());
    return R.ok();
  }

  /**
   * 同步导出用户 xlsx。
   *
   * @param query    导出筛选条件
   * @param response 文件流
   */
  @Operation(summary = "导出用户")
  @SaCheckPermission("system:user:export")
  @PostMapping("export")
  public void export(@RequestBody(required = false) SysUserVo query, HttpServletResponse response) throws Exception {
    ExcelUtils.exportExcel(userService.export(query), "用户", SysUserVo.class, response);
  }

  /**
   * 下载用户导入 Excel 模板。
   *
   * @param response 文件流
   */
  @Operation(summary = "导入模板")
  @SaCheckPermission("system:user:import")
  @GetMapping("import/template")
  public void template(HttpServletResponse response) {
    ExcelUtils.exportExcel(Collections.emptyList(), "user-import-template", SysUserImportRow.class, false, true, response);
  }

  /**
   * 同步导入用户；可选更新已存在用户名。
   *
   * @param file          Excel 文件
   * @param updateSupport 是否更新已存在数据（true/1）
   * @return 导入统计与失败行
   */
  @Operation(summary = "导入用户")
  @SaCheckPermission("system:user:import")
  @Idempotent(ttlSeconds = 10, key = "#userId + ':import:user'", message = "请勿重复提交")
  @PostMapping("import")
  public R<ExcelResult<SysUserImportRow>> importExcel(
    @RequestParam("file") MultipartFile file,
    @RequestParam(value = "updateSupport", defaultValue = "false") String updateSupport) throws IOException {
    boolean update = "true".equalsIgnoreCase(updateSupport) || "1".equals(updateSupport);
    return R.ok(userService.importExcel(file, update));
  }

  @Data
  public static class ChangeStatusBody {
    private Long userId;
    private String status;
  }

  @Data
  public static class ResetPwdBody {
    private Long userId;
    private String password;
  }

  @Data
  public static class AuthRoleBody {
    private Long userId;
    private List<Long> roleIds;
  }
}
