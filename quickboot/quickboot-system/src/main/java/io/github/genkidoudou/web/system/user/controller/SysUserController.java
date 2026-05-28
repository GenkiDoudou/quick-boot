package io.github.genkidoudou.web.system.user.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.github.genkidoudou.web.system.user.dto.SysUserCreateBo;
import io.github.genkidoudou.web.system.user.dto.SysUserDetailVo;
import io.github.genkidoudou.web.system.user.dto.SysUserQueryBo;
import io.github.genkidoudou.web.system.user.dto.SysUserUpdateBo;
import io.github.genkidoudou.web.system.user.dto.SysUserVo;
import io.github.genkidoudou.web.system.user.dto.UserAuthRoleRequest;
import io.github.genkidoudou.web.system.user.dto.UserAuthRoleVo;
import io.github.genkidoudou.web.system.user.dto.UserChangeStatusRequest;
import io.github.genkidoudou.web.system.user.dto.UserImportResultVo;
import io.github.genkidoudou.web.system.user.dto.UserResetPwdRequest;
import io.github.genkidoudou.web.system.user.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 系统用户管理接口。
 */
@Tag(name = "用户管理")
@Validated
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @Operation(summary = "用户分页列表")
    @SaCheckPermission("system:user:list")
    @GetMapping("/list")
    public R<PageInfo<SysUserVo>> list(@Validated SysUserQueryBo query) {
        return R.ok(userService.page(query));
    }

    @Operation(summary = "用户详情")
    @SaCheckPermission("system:user:list")
    @GetMapping("/{userId}")
    public R<SysUserDetailVo> get(@Parameter(description = "用户ID") @PathVariable @Min(1) Long userId) {
        SysUserDetailVo vo = userService.get(userId);
        if (vo == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "用户不存在或已删除");
        }
        return R.ok(vo);
    }

    @Operation(summary = "新增用户")
    @SaCheckPermission("system:user:add")
    @PostMapping("/create")
    public R<Void> create(@Validated(AddGroup.class) @RequestBody SysUserCreateBo req) {
        userService.create(req);
        return R.ok();
    }

    @Operation(summary = "修改用户")
    @SaCheckPermission("system:user:edit")
    @PostMapping("/update")
    public R<Void> update(@Validated(UpdateGroup.class) @RequestBody SysUserUpdateBo req) {
        userService.update(req);
        return R.ok();
    }

    @Operation(summary = "删除用户（支持批量）")
    @SaCheckPermission("system:user:remove")
    @PostMapping("/remove")
    public R<Void> remove(@RequestBody List<Long> userIds) {
        userService.remove(userIds);
        return R.ok();
    }

    @Operation(summary = "修改用户状态")
    @SaCheckPermission("system:user:edit")
    @PostMapping("/changeStatus")
    public R<Void> changeStatus(@Validated @RequestBody UserChangeStatusRequest req) {
        userService.changeStatus(req);
        return R.ok();
    }

    @Operation(summary = "重置用户密码")
    @SaCheckPermission("system:user:resetPwd")
    @PostMapping("/resetPwd")
    public R<Void> resetPwd(@Validated @RequestBody UserResetPwdRequest req) {
        userService.resetPwd(req);
        return R.ok();
    }

    @Operation(summary = "分配角色页数据")
    @SaCheckPermission("system:user:edit")
    @GetMapping("/authRole/{userId}")
    public R<UserAuthRoleVo> authRole(@Parameter(description = "用户ID") @PathVariable @Min(1) Long userId) {
        return R.ok(userService.authRoleInfo(userId));
    }

    @Operation(summary = "保存用户角色分配")
    @SaCheckPermission("system:user:edit")
    @PostMapping("/authRole")
    public R<Void> saveAuthRole(@Validated @RequestBody UserAuthRoleRequest req) {
        userService.saveAuthRole(req);
        return R.ok();
    }

    @Operation(summary = "导入用户数据")
    @SaCheckPermission("system:user:import")
    @PostMapping(value = "/importData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<UserImportResultVo> importData(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "updateSupport", defaultValue = "false") boolean updateSupport) {
        return R.ok(userService.importData(file, updateSupport));
    }

    @Operation(summary = "下载用户导入模板")
    @SaCheckPermission("system:user:import")
    @GetMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        userService.importTemplate(response);
    }

    @Operation(summary = "下载用户导入失败明细")
    @SaCheckPermission("system:user:import")
    @GetMapping("/importError")
    public void importError(
            @Parameter(description = "导入结果返回的 errorKey") @RequestParam String errorKey,
            HttpServletResponse response) {
        userService.importError(errorKey, response);
    }

    @Operation(summary = "导出用户")
    @SaCheckPermission("system:user:export")
    @PostMapping("/export")
    public void export(@ModelAttribute SysUserQueryBo query, HttpServletResponse response) {
        userService.export(query, response);
    }
}
