package io.github.genkidoudou.system.internal.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单树节点（角色授权用）。
 */
@Data
public class SysMenuTreeVo {

  private Long menuId;

  private Long parentId;

  private String menuName;

  private String menuType;

  private List<SysMenuTreeVo> children = new ArrayList<>();
}
