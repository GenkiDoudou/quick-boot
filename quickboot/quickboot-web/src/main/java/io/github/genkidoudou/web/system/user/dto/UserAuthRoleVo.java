package io.github.genkidoudou.web.system.user.dto;

import io.github.genkidoudou.web.system.role.dto.SysRoleVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 分配角色页回显：可选角色列表 + 已勾选 id。
 */
@Data
@Schema(description = "用户分配角色页数据")
public class UserAuthRoleVo {

    @Schema(description = "用户 id")
    private Long userId;

    @Schema(description = "登录账号")
    private String userName;

    @Schema(description = "用户昵称")
    private String nickName;

    @Schema(description = "全部可选角色（简要）")
    private List<SysRoleVo> roles;

    @Schema(description = "已绑定角色 id")
    private List<Long> roleIds;
}
