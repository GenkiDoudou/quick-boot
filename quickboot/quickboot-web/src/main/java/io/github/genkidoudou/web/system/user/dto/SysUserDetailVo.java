package io.github.genkidoudou.web.system.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户详情（含角色 id 列表，供表单回显）。
 */
@Data
@Schema(description = "用户详情")
public class SysUserDetailVo {

    @Schema(description = "用户 id")
    private Long userId;

    @Schema(description = "登录账号")
    private String userName;

    @Schema(description = "用户昵称")
    private String nickName;

    @Schema(description = "部门 id")
    private Long deptId;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phonenumber;

    @Schema(description = "性别")
    private String sex;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "已绑定角色 id 列表")
    private List<Long> roleIds;
}
