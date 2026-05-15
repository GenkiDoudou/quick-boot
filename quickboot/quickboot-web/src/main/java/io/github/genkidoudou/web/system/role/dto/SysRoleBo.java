package io.github.genkidoudou.web.system.role.dto;

import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 角色新增/修改入参；菜单权限仍走独立接口，数据权限可随表单一并提交。
 */
@Data
@Schema(description = "角色业务入参")
public class SysRoleBo {

    @NotNull(message = "角色ID不能为空", groups = UpdateGroup.class)
    @Schema(description = "角色ID（修改必填）")
    private Long roleId;

    @NotBlank(message = "角色名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 30, message = "角色名称长度不能超过30", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "角色名称")
    private String roleName;

    @NotBlank(message = "权限字符不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 100, message = "权限字符长度不能超过100", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "权限字符 role_key")
    private String roleKey;

    @NotNull(message = "显示顺序不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "显示顺序")
    private Integer roleSort;

    @Pattern(regexp = "^[01]$", message = "状态必须为0或1", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "状态：0 正常 1 停用")
    private String status;

    @Size(max = 500, message = "备注长度不能超过500", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "备注")
    private String remark;

    @Schema(description = "数据范围：1 全部 2 自定义 3 本部门 4 本部门及以下 5 仅本人；不传时新增默认全部")
    private String dataScope;

    @Schema(description = "自定义数据权限时的部门 id 列表（dataScope=2 时由服务端校验非空）")
    private List<Long> deptIds;
}
