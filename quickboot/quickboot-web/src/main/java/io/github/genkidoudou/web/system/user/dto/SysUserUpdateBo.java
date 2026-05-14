package io.github.genkidoudou.web.system.user.dto;

import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 修改用户入参。
 */
@Data
@Schema(description = "修改用户")
public class SysUserUpdateBo {

    @NotNull(message = "用户ID不能为空", groups = UpdateGroup.class)
    @Schema(description = "用户 id")
    private Long userId;

    @NotBlank(message = "用户昵称不能为空", groups = UpdateGroup.class)
    @Size(max = 30, message = "用户昵称长度不能超过30", groups = UpdateGroup.class)
    @Schema(description = "用户昵称")
    private String nickName;

    @Size(min = 6, max = 100, message = "密码长度须在6到100之间", groups = UpdateGroup.class)
    @Schema(description = "新密码；不传或空表示不修改")
    private String password;

    @NotNull(message = "部门不能为空", groups = UpdateGroup.class)
    @Schema(description = "部门 id")
    private Long deptId;

    @Size(max = 50, message = "邮箱长度不能超过50", groups = UpdateGroup.class)
    @Schema(description = "邮箱")
    private String email;

    @Size(max = 11, message = "手机号长度不能超过11", groups = UpdateGroup.class)
    @Schema(description = "手机号")
    private String phonenumber;

    @Pattern(regexp = "^[012]?$", message = "性别取值无效", groups = UpdateGroup.class)
    @Schema(description = "性别")
    private String sex;

    @Pattern(regexp = "^[01]$", message = "状态必须为0或1", groups = UpdateGroup.class)
    @Schema(description = "状态")
    private String status;

    @Size(max = 500, message = "备注长度不能超过500", groups = UpdateGroup.class)
    @Schema(description = "备注")
    private String remark;

    @NotEmpty(message = "至少选择一个角色", groups = UpdateGroup.class)
    @Schema(description = "角色 id 列表")
    private List<Long> roleIds;
}
