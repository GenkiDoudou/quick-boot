package io.github.genkidoudou.web.system.menu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 角色菜单树勾选数据（若依风格）。
 */
@Data
@Schema(description = "角色菜单树勾选")
public class RoleMenuTreeselectVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<SysMenuTreeSelectVo> menus;

    @Schema(description = "角色已勾选菜单 id")
    private List<Long> checkedKeys;
}
