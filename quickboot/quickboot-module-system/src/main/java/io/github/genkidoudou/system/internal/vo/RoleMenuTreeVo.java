package io.github.genkidoudou.system.internal.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色菜单树响应：全树 + 已选 keys。
 */
@Data
public class RoleMenuTreeVo {

  private List<SysMenuTreeVo> menus = new ArrayList<>();

  private List<Long> checkedKeys = new ArrayList<>();
}
