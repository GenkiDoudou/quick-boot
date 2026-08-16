package io.github.genkidoudou.system.internal.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色菜单树响应：全树 + 已选 keys。
 */
@Data
public class RoleMenuTreeVo {

  /** 全量菜单树。 */
  private List<SysMenuTreeVo> menus = new ArrayList<>();

  /** 已勾选菜单 ID。 */
  private List<Long> checkedKeys = new ArrayList<>();
}
