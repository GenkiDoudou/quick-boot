package com.su60.quickboot.system.service;

import cn.hutool.core.lang.tree.Tree;
import com.su60.quickboot.system.dos.RouterVo;
import com.su60.quickboot.system.dos.SysMenuDo;
import com.su60.quickboot.system.dos.TreeSelect;
import com.su60.quickboot.system.entity.SysMenuEntity;

import java.util.List;

/**
 * <p>
 * 菜单权限表 服务类
 * </p>
 *
 * @author luyanan
 * @since 2024/08/07
 */

public interface ISysMenuService {

	/**
	 * 根据角色id查询权限列表
	 *
	 * @param roleIds 角色id集合
	 * @return 权限列表
	 * @since 2024/8/7
	 */
	List<String> listPermsByRoleIds(List<Long> roleIds);


	/**
	 * 根据角色id查询菜单
	 *
	 * @param roleIds 角色id集合
	 * @return 菜单
	 * @since 2024/8/7
	 */
	List<SysMenuDo> listByRoleIds(List<Long> roleIds);

	/**
	 * 根据用户id查询路由信息
	 *
	 * @param userId 用户id
	 * @return 路由信息
	 * @since 2024/8/7
	 */
	List<RouterVo> getRouters(Long userId);

	/**
	 * 获取菜单下拉树列表
	 *
	 * @param sysMenuDo 菜单信息
	 * @param userId    用户id
	 * @return 菜单下拉树列表
	 * @since 2024/8/20
	 */
	List<TreeSelect> treeselect(SysMenuDo sysMenuDo, Long userId);

	/**
	 * 根据用户id查询菜单列表
	 *
	 * @param userId    用户id
	 * @param sysMenuDo 条件
	 * @return 菜单列表
	 * @since 2024/8/20
	 */
	List<SysMenuDo> listByUserId(Long userId, SysMenuDo sysMenuDo);

	/**
	 * 构建前端所需要的下拉树结构
	 *
	 * @param sysMenuDos 菜单列表
	 * @return 下拉树结构
	 * @since 2024/8/20
	 */
	List<TreeSelect> buildMenuTreeSelect(List<SysMenuDo> sysMenuDos);

	/**
	 * 根据角色id查询菜单树信息
	 *
	 * @param roleId 角色id
	 * @return 菜单树信息
	 * @since 2024/10/10
	 */
	List<Long> selectMenuListByRoleId(Long roleId);


	/**
	 * 查找父级
	 *
	 * @param id id
	 * @return 列表
	 */
	List<Long> findParent(Long id);


	/**
	 * 树形菜单列表
	 * @since 2025/12/23
	 * @param sysMenuDo 参数
	 * @param userId  用户id
	 * @return
	 */
	List<Tree<Long>> treeList(SysMenuDo sysMenuDo, Long userId);


	/**
	 * 根据id查询
	 * @since 2025/12/30
	 * @param id id
	 * @return
	 */
	SysMenuDo getVoById(Long id);

	/**
	 * 保存
	 * @since 2025/12/30
	 * @param sysMenuDo
	 * @return
	 */
	Boolean saveVo(SysMenuDo sysMenuDo);

	/**
	 * 根据id修改
	 * @since 2025/12/30
	 * @param sysMenuDo
	 * @return
	 */
	Boolean updateVoById(SysMenuDo sysMenuDo);

	/**
	 * 根据id删除
	 * @since 2025/12/30
	 * @param id  id
	 * @return
	 */
	Boolean deleteById(Long id);
}
