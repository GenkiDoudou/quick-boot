package com.su60.quickboot.system.service;

import com.su60.quickboot.system.entity.SysRoleDeptEntity;

import java.util.List;

/**
 * <p>
 * 角色关联部门表 服务类
 * </p>
 *
 * @author luyanan
 * @since 2025/12/27
 */

public interface ISysRoleDeptService {


	/**
	 * 根据角色id集合查询关联的部门id集合
	 * @since 2025/12/27
	 * @param roleIds  角色id集合
	 * @return
	 */
	List<Long> listDeptByRoleIds(List<Long> roleIds);


	/**
	 * 根据角色id查询关联的部门id
	 * @since 2026/1/3
	 * @param roleId
	 * @return
	 */
	List<Long> listDeptByRoleId(Long roleId);


	/**
	 * 保存
	 * @since 2026/1/6
	 * @param roleId  角色id
	 * @param deptIds 部门id集合
	 * @return
	 */

	void save(Long roleId, List<Long> deptIds);

	/**
	 * 根据角色id集合删除
	 * @since 2026/1/6
	 * @param roleIds 角色id 集合
	 * @return
	 */
	void deleteByRoleIds(List<Long> roleIds);
}
