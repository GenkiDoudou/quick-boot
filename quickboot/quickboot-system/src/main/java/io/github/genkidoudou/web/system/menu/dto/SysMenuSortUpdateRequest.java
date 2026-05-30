package io.github.genkidoudou.web.system.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 菜单树行内排序批量保存。
 */
@Data
@Schema(description = "菜单排序批量更新")
public class SysMenuSortUpdateRequest {

    @NotEmpty(message = "menuIds 不能为空")
    @Schema(description = "菜单 ID 列表，与 orderNums 一一对应")
    private List<Long> menuIds;

    @NotEmpty(message = "orderNums 不能为空")
    @Schema(description = "显示排序列表")
    private List<@NotNull Integer> orderNums;
}
