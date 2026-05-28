package io.github.genkidoudou.web.system.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 菜单新增/修改入参。
 */
@Data
@Schema(description = "菜单新增或修改请求")
public class SysMenuSaveRequest {

    @Schema(description = "菜单ID，新增可为空，修改必填")
    private Long menuId;

    @NotNull(message = "上级菜单不能为空")
    @Schema(description = "上级菜单ID，顶级为-1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long parentId;

    @NotBlank(message = "菜单类型不能为空")
    @Pattern(regexp = "^[MCF]$", message = "菜单类型须为 M、C 或 F")
    @Schema(description = "菜单类型：M目录 C菜单 F按钮", requiredMode = Schema.RequiredMode.REQUIRED)
    private String menuType;

    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50")
    @Schema(description = "菜单名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String menuName;

    @NotNull(message = "显示顺序不能为空")
    @Max(value = 9999, message = "显示顺序不能超过9999")
    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer orderNum;

    @Size(max = 200, message = "路由地址长度不能超过200")
    @Schema(description = "路由地址")
    private String path;

    @Size(max = 255, message = "组件路径长度不能超过255")
    @Schema(description = "组件路径")
    private String component;

    @Size(max = 255, message = "路由参数长度不能超过255")
    @Schema(description = "路由参数")
    private String query;

    @Size(max = 100, message = "路由名称长度不能超过100")
    @Schema(description = "路由名称")
    private String routeName;

    @Pattern(regexp = "^[01]$", message = "是否外链须为0或1")
    @Schema(description = "是否外链：0否 1是")
    private String isFrame;

    @Pattern(regexp = "^[01]$", message = "是否缓存须为0或1")
    @Schema(description = "是否缓存：0缓存 1不缓存")
    private String isCache;

    @Pattern(regexp = "^[01]$", message = "显示状态须为0或1")
    @Schema(description = "显示状态：0显示 1隐藏", requiredMode = Schema.RequiredMode.REQUIRED)
    private String visible;

    @Pattern(regexp = "^[01]$", message = "菜单状态须为0或1")
    @Schema(description = "菜单状态：0正常 1停用", requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;

    @Size(max = 500, message = "权限标识总长度不能超过500（多个以英文逗号分隔）")
    @Schema(description = "权限标识，多个以英文逗号分隔，如 system:menu:list,system:menu:add")
    private String perms;

    @Size(max = 100, message = "图标长度不能超过100")
    @Schema(description = "图标")
    private String icon;

    @Size(max = 500, message = "备注长度不能超过500")
    @Schema(description = "备注")
    private String remark;
}
