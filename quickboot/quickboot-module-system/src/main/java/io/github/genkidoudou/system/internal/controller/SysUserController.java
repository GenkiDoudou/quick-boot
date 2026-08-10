package io.github.genkidoudou.system.internal.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.desensitization.SensitiveResponse;
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

@Tag(name = "用户管理")
@RequiredArgsConstructor
@RestController
@RequestMapping("sys/user")
public class SysUserController {

  private final ISysUserService userService;

  @Operation(summary = "分页查询")
  @SaCheckPermission("system:user:list")
  @SensitiveResponse
  @PostMapping("page")
  public R<PageInfo<SysUserVo>> page(@RequestBody PageRequest<SysUserVo> pageRequest) {
    return R.ok(userService.page(pageRequest));
  }

  @Operation(summary = "用户详情")
  @SaCheckPermission(value = {"system:user:query", "system:user:list", "system:user:edit"}, mode = SaMode.OR)
  @GetMapping("/{userId}")
  public R<SysUserVo> get(@PathVariable Long userId) {
    return R.ok(userService.getDetail(userId));
  }

  @Operation(summary = "新增用户")
  @SaCheckPermission("system:user:add")
  @PostMapping("add")
  public R<String> add(@RequestBody @Validated(AddGroup.class) SysUserVo vo) {
    Long id = userService.add(vo);
    return R.ok(id == null ? null : String.valueOf(id));
  }

  @Operation(summary = "修改用户")
  @SaCheckPermission("system:user:edit")
  @PostMapping("update")
  public R<Boolean> update(@RequestBody @Validated(UpdateGroup.class) SysUserVo vo) {
    return R.ok(userService.update(vo));
  }

  @Operation(summary = "删除用户")
  @SaCheckPermission("system:user:remove")
  @GetMapping("remove/{userId}")
  public R<Void> removeGet(@PathVariable Long userId) {
    userService.remove(List.of(userId));
    return R.ok();
  }

  @Operation(summary = "批量删除用户")
  @SaCheckPermission("system:user:remove")
  @PostMapping("remove")
  public R<Void> remove(@RequestBody List<Long> userIds) {
    userService.remove(userIds);
    return R.ok();
  }

  @Operation(summary = "修改用户状态")
  @SaCheckPermission("system:user:edit")
  @PostMapping("changeStatus")
  public R<Void> changeStatus(@RequestBody ChangeStatusBody body) {
    userService.changeStatus(body.getUserId(), body.getStatus());
    return R.ok();
  }

  @Operation(summary = "重置密码")
  @SaCheckPermission("system:user:resetPwd")
  @PostMapping("resetPwd")
  public R<Void> resetPwd(@RequestBody ResetPwdBody body) {
    userService.resetPwd(body.getUserId(), body.getPassword());
    return R.ok();
  }

  @Operation(summary = "查询授权角色")
  @SaCheckPermission("system:user:edit")
  @GetMapping("authRole/{userId}")
  public R<SysUserAuthRoleVo> authRole(@PathVariable Long userId) {
    return R.ok(userService.authRole(userId));
  }

  @Operation(summary = "保存授权角色")
  @SaCheckPermission("system:user:edit")
  @PostMapping("authRole")
  public R<Void> saveAuthRole(@RequestBody AuthRoleBody body) {
    userService.saveAuthRole(body.getUserId(), body.getRoleIds());
    return R.ok();
  }

  @Operation(summary = "导出用户")
  @SaCheckPermission("system:user:export")
  @PostMapping("export")
  public void export(@RequestBody(required = false) SysUserVo query, HttpServletResponse response) throws Exception {
    ExcelUtils.exportExcel(userService.export(query), "用户", SysUserVo.class, response);
  }

  @Operation(summary = "导入模板")
  @SaCheckPermission("system:user:import")
  @GetMapping("import/template")
  public void template(HttpServletResponse response) {
    ExcelUtils.exportExcel(Collections.emptyList(), "user-import-template", SysUserImportRow.class, false, true, response);
  }

  @Operation(summary = "导入用户")
  @SaCheckPermission("system:user:import")
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
