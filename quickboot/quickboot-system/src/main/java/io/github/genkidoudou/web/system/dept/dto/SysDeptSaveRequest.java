package io.github.genkidoudou.web.system.dept.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 部门新增/修改入参。
 */
@Data
@Schema(description = "部门新增或修改请求")
public class SysDeptSaveRequest {

    @Schema(description = "部门ID，新增可为空，修改必填")
    private Long deptId;

    @NotNull(message = "上级部门不能为空")
    @Schema(description = "上级部门ID，顶级为-1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long parentId;

    @NotBlank(message = "部门名称不能为空")
    @Size(max = 30, message = "部门名称长度不能超过30")
    @Schema(description = "部门名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String deptName;

    @NotNull(message = "显示顺序不能为空")
    @Max(value = 9999, message = "显示顺序不能超过9999")
    @Schema(description = "同级显示顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer orderNum;

    @Size(max = 20, message = "负责人长度不能超过20")
    @Schema(description = "负责人")
    private String leader;

    @Pattern(regexp = "^$|^[\\d\\-+\\s()]{5,20}$", message = "联系电话格式不正确")
    @Schema(description = "联系电话")
    private String phone;

    @Pattern(regexp = "^$|^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;

    @Pattern(regexp = "^[01]$", message = "状态取值须为0或1")
    @Schema(description = "状态：0正常，1停用", requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;

    @Size(max = 200, message = "备注长度不能超过200")
    @Schema(description = "备注")
    private String remark;
}
