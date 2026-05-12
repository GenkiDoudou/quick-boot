package com.su60.quickboot.system.service;

import com.su60.quickboot.data.mybatisplus.BaseVoServiceImpl;
import com.su60.quickboot.system.dos.SysRoleMenuDo;
import com.su60.quickboot.system.entity.SysRoleMenuEntity;

import java.util.List;

/**
 * <p>
 * 角色和菜单关联表 服务类
 * </p>
 *
 * @author luyanan
 * @since 2024/08/07
 */

public interface ISysRoleMenuService  {

	/**
	 * 保存角色菜单关联关系
	 *
	 * @param roleId  角色id
	 * @param menuIds 菜单列表id
	 * @since 2024/8/22
	 */
	void save(Long roleId, List<Long> menuIds);

	/**
	 * 根据角色id集合删除
	 *
	 * @param roleIds 角色id集合
	 * @since 2024/08/24
	 */
	void delete(List<Long> roleIds);

	/**
	 * 根据角色id集合查询
	 *
	 * @param roleIds 角色id集合
	 * @return 集合
	 * @since 2024/08/24
	 */
	List<SysRoleMenuEntity> list(List<Long> roleIds);

	/**
	 * 检查菜单是否绑定角色
	 *
	 * @param menuId 菜单id
	 * @return 是否绑定
	 * @since 2024/11/6
	 */
	boolean checkMenuExistRole(Long menuId);
}
