package io.github.genkidoudou.system.service;

import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.system.vo.MenuSortVo;
import io.github.genkidoudou.system.vo.MenuTreeSelectVo;
import io.github.genkidoudou.system.vo.SysMenuImportRow;
import io.github.genkidoudou.system.vo.SysMenuVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 系统菜单管理。
 */
public interface ISysMenuService {

  /**
   * 菜单树列表（可按名称、状态过滤）。
   *
   * @param menuName 名称模糊，可空
   * @param status   状态，可空
   * @return 树
   */
  List<SysMenuVo> listTree(String menuName, String status);

  /**
   * 下拉树。
   *
   * @param excludeButton 是否排除按钮 F
   * @param directoryOnly 是否仅目录 M
   * @return 树节点
   */
  List<MenuTreeSelectVo> treeselect(boolean excludeButton, boolean directoryOnly);

  /**
   * 角色菜单树（含已勾选）。
   *
   * @param roleId 角色 id
   * @return menus + checkedKeys
   */
  Map<String, Object> roleMenuTreeselect(Long roleId);

  /**
   * 详情。
   *
   * @param menuId 主键
   * @return Vo
   */
  SysMenuVo getDetail(Long menuId);

  /**
   * 新增。
   *
   * @param vo 可写字段
   * @return 新建 menuId
   */
  Long add(SysMenuVo vo);

  /**
   * 修改。
   *
   * @param vo 含 menuId
   * @return 是否成功
   */
  boolean update(SysMenuVo vo);

  /**
   * 批量更新排序。
   *
   * @param sort 菜单 id 与 orderNum 平行数组
   */
  void updateSort(MenuSortVo sort);

  /**
   * 按主键删除（有子则拒绝）；空集合静默返回。
   *
   * @param menuIds 主键集合
   */
  void remove(Collection<Long> menuIds);

  /**
   * 根据角色id集合查询关联的菜单列表
   *
   * @param roleIds 角色id集合
   * @return 菜单列表
   * @since 2026/8/2
   */
  List<SysMenuVo> listByRoles(List<Long> roleIds);

  /**
   * 同步导出（扁平行）。有 ids 则仅导出勾选；否则按 menuName/status。
   *
   * @param query 导出条件
   * @return 导出行
   */
  List<SysMenuVo> export(SysMenuVo query);


}
