package com.su60.quickboot.system.service;

import com.su60.quickboot.data.mybatisplus.IBaseService2;
import com.su60.quickboot.system.dos.SysRoleDo;
import com.su60.quickboot.system.entity.SysRoleEntity;
import com.su60.quickboot.system.excel.SysRoleExcel;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 角色信息表 服务类
 * </p>
 *
 * @author luyanan
 * @since 2024/08/07
 */

public interface ISysRoleService extends IBaseService2<SysRoleEntity, SysRoleDo> {


	/**
	 * 根据用户id查询关联的角色列表
	 *
	 * @param userId 用户id
	 * @return 角色列表
	 * @since 2024/8/7
	 */
	List<SysRoleDo> listByUserId(Long userId);

	/**
	 * 角色保存
	 *
	 * @param sysRoleDo 角色信息
	 * @return 是否成功
	 * @since 2024/8/21
	 */
	boolean saveRole(SysRoleDo sysRoleDo);

	/**
	 * 根据id集合删除
	 *
	 * @param ids id集合
	 * @return 是否成功
	 * @since 2024/08/24
	 */
	boolean delete(List<Long> ids);

	/**
	 * 根据条件查询
	 *
	 * @param sysRoleEntity 查询条件
	 * @return 角色列表
	 * @since 2024/08/24
	 */
	List<SysRoleEntity> list(SysRoleEntity sysRoleEntity);

	/**
	 * 该角色选中的菜单id
	 *
	 * @param roleId 角色id
	 * @return 菜单id列表
	 * @since 2024/08/24
	 */
	List<Long> checkedKeys(Long roleId);

	/**
	 * 修改角色
	 *
	 * @param sysRoleDo 角色信息
	 * @return 是否成功
	 * @since 2024/10/10
	 */
	boolean updateRole(SysRoleDo sysRoleDo);


	/**
	 * 根据用户id查询关联的角色id
	 *
	 * @param userId 用户id
	 * @return 角色id列表
	 * @since 2024/10/14
	 */
	List<Long> listRoleIdUserId(Long userId);


	/**
	 * 保存用户角色关联关系
	 *
	 * @param userId  用户id
	 * @param roleIds 角色id集合
	 * @since 2024/10/14
	 */
	void saveUserRoles(Long userId, List<Long> roleIds);

	/**
	 * 展示所有的角色
	 *
	 * @param sysRoleDo 条件
	 * @return 角色列表
	 * @since 2024/10/31
	 */
	List<SysRoleDo> listAll(SysRoleDo sysRoleDo);

	/**
	 * 导出
	 * @since 2025/12/19
	 * @param response
	 * @param sysRoleDo
	 * @return
	 */
	void export(HttpServletResponse response, SysRoleDo sysRoleDo) throws Exception;

}
