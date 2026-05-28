package io.github.genkidoudou.web.system.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 角色分页列表查询参数（与前端分页字段名一致）。
 */
@Data
@Schema(description = "角色列表查询参数")
public class SysRoleQueryBo {

    @Min(1)
    @Schema(description = "页码，从 1 开始")
    private Integer pageNum = 1;

    @Min(1)
    @Schema(description = "每页条数")
    private Integer pageSize = 10;

    @Schema(description = "角色名称（模糊）")
    private String roleName;

    @Schema(description = "权限字符 role_key（模糊）")
    private String roleKey;

    @Schema(description = "状态（精确）")
    private String status;

    @Schema(description = "创建时间起（yyyy-MM-dd）")
    private String beginTime;

    @Schema(description = "创建时间止（yyyy-MM-dd）")
    private String endTime;
}
