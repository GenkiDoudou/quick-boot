package io.github.genkidoudou.web.system.menu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单下拉树节点（id/label/children）。
 */
@Data
@Schema(description = "菜单下拉树节点")
public class SysMenuTreeSelectVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String label;

    @Schema(description = "子节点")
    private List<SysMenuTreeSelectVo> children = new ArrayList<>();
}
