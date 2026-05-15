package io.github.genkidoudou.web.system.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色列表/详情出参。
 */
@Data
@Schema(description = "角色视图对象")
public class SysRoleVo {

    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "权限字符")
    private String roleKey;

    @Schema(description = "显示顺序")
    private Integer roleSort;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "数据范围")
    private String dataScope;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "自定义数据权限时勾选的部门 id 列表")
    private List<Long> deptIds;
}
