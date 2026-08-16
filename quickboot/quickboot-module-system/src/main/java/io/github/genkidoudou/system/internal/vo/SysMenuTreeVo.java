package io.github.genkidoudou.system.internal.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单树节点（角色授权用）。
 */
@Data
public class SysMenuTreeVo {

  /** 菜单主键。 */
  private Long menuId;

  /** 上级菜单 ID。 */
  private Long parentId;

  /** 菜单名称。 */
  private String menuName;

  /** 菜单类型(sys_menu_menu_type)。 */
  private String menuType;

  /** 子节点。 */
  private List<SysMenuTreeVo> children = new ArrayList<>();
}
