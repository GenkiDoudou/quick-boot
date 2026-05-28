package io.github.genkidoudou.web.system.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 用户分页列表查询参数。
 */
@Data
@Schema(description = "用户列表查询参数")
public class SysUserQueryBo {

    @Min(1)
    @Schema(description = "页码，从 1 开始")
    private Integer pageNum = 1;

    @Min(1)
    @Schema(description = "每页条数")
    private Integer pageSize = 10;

    @Schema(description = "用户名（模糊）")
    private String userName;

    @Schema(description = "用户昵称（模糊）")
    private String nickName;

    @Schema(description = "手机号（模糊）")
    private String phonenumber;

    @Schema(description = "状态（精确）")
    private String status;

    @Schema(description = "创建时间起（yyyy-MM-dd）")
    private String beginTime;

    @Schema(description = "创建时间止（yyyy-MM-dd）")
    private String endTime;

    @Schema(description = "部门 id（含子孙部门）")
    private Long deptId;
}
