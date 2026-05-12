package com.su60.quickboot.system.service;

import com.su60.quickboot.system.entity.SysUserRoleEntity;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 用户和角色关联表 服务类
 * </p>
 *
 * @author luyanan
 * @since 2024/08/07
 */

public interface ISysUserRoleService  {

	/**
	 * 根据条件查询
	 *
	 * @param sysUserRoleEntity 查询条件
	 * @return 用户角色关联关系列表
	 * @since 2024/08/24
	 */
	List<SysUserRoleEntity> list(SysUserRoleEntity sysUserRoleEntity);

	/**
	 * 保存用户和角色的关联关系
	 *
	 * @param userId  用户id
	 * @param roleIds 角色id集合
	 * @since 2024/10/14
	 */
	void saveUserRoles(Long userId, List<Long> roleIds);

	/**
	 * 根据用户id删除
	 *
	 * @param userIds 用户id
	 * @since 2024/10/31
	 */
	void deleteByUserIds(Collection<? extends Serializable> userIds);

	/**
	 * 根据用户id查询关联的角色关系
	 *
	 * @param userId 用户id
	 * @return  用户角色关联关系列表
	 * @since 2024/10/31
	 */
	List<SysUserRoleEntity> listByUserId(Serializable userId);
}
