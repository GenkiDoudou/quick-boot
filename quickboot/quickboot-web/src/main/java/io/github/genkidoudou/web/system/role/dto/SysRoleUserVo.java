package io.github.genkidoudou.web.system.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色分配用户列表中的用户行。
 */
@Data
@Schema(description = "角色关联用户视图")
public class SysRoleUserVo {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "登录账号")
    private String userName;

    @Schema(description = "昵称")
    private String nickName;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
