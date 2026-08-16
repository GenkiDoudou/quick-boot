package io.github.genkidoudou.system.internal.service;

import io.github.genkidoudou.system.internal.entity.SysMenu;
import io.github.genkidoudou.system.internal.vo.H5WorkbenchGroupVo;
import io.github.genkidoudou.system.internal.vo.H5WorkbenchItemVo;

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
   * 当前用户可见的 H5 页面入口（扁平）：menu_type=C、path 以 {@code /pages/} 开头、未隐藏。
   * <p>工作台分组与首页快捷候选共用本列表，避免过滤规则分叉。</p>
   *
   * @param userId 用户主键字符串
   * @return 按菜单 order_num 排序的入口
   */
  List<H5WorkbenchItemVo> listH5PageItems(String userId);

  /**
   * 组装 quick-h5 工作台分组：仅含 path 以 {@code /pages/} 开头的授权菜单 C，
   * 按父级目录 M 分组；按钮 F 不出现。
   *
   * @param userId 用户主键字符串
   * @return 分组列表
   */
  List<H5WorkbenchGroupVo> buildH5Workbench(String userId);

  /**
   * 全部启用未删菜单（含按钮），按 order_num 排序。
   */
  List<SysMenu> listAllEnabledMenus();

  /**
   * 角色已绑定 menuId。
   */
  List<Long> listMenuIdsByRoleId(Long roleId);
}
