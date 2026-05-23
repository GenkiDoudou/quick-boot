package com.su60.quickboot.system.mapper;

import com.su60.quickboot.system.entity.SysRoleEntity;
import com.su60.quickboot.data.mybatisplus.BaseBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 角色信息表 Mapper 接口
 * </p>
 *
 * @author luyanan
 * @since 2024/08/07
 */
@Mapper
public interface SysRoleMapper extends BaseBaseMapper<SysRoleEntity> {

	/**
	 * 根据角色id查询菜单树信息
	 *
	 * @param roleId            角色id
	 * @param menuCheckStrictly 菜单树选择项是否关联显示
	 * @return  菜单树信息
	 * @since 2024/08/24
	 */
	List<Long> selectMenuListByRoleId(@Param("roleId") Long roleId, @Param("menuCheckStrictly") Integer menuCheckStrictly);
}

