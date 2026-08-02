package io.github.genkidoudou.system.service;

import io.github.genkidoudou.system.entity.SysMenu;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 登录 RBAC：角色键、权限字符、动态路由。
 */
public interface ISysPermissionService {

  /**
   * @param userId 用户主键字符串
   * @return 启用未删角色的 role_key 列表
   */
  List<String> listRoleKeys(String userId);

  /**
   * @param userId 用户主键字符串
   * @return 按角色菜单绑定汇总的权限字符集合（含逗号拆分）；无特权短路
   */
  Set<String> listPermissions(String userId);

  /**
   * 组装若依形态路由树（仅 M/C）。
   *
   * @param userId 用户主键字符串
   * @return routers
   */
  List<Map<String, Object>> buildRouters(String userId);

  /**
   * 全部启用未删菜单（含按钮），按 order_num 排序。
   */
  List<SysMenu> listAllEnabledMenus();

  /**
   * 角色已绑定 menuId。
   */
  List<Long> listMenuIdsByRoleId(Long roleId);
}
