package io.github.genkidoudou.web.system.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户列表行视图对象。
 */
@Data
@Schema(description = "用户列表行")
public class SysUserVo {

    @Schema(description = "用户 id")
    private Long userId;

    @Schema(description = "登录账号")
    private String userName;

    @Schema(description = "用户昵称")
    private String nickName;

    @Schema(description = "部门 id")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "手机号")
    private String phonenumber;

    @Schema(description = "状态：0 正常 1 停用")
    private String status;

    @Schema(description = "角色名称聚合")
    private String roleNames;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
