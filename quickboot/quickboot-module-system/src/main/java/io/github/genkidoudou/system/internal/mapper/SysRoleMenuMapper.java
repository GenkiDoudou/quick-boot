package io.github.genkidoudou.system.internal.mapper;

import io.github.genkidoudou.common.mybatisplus.BaseBaseMapper;

import io.github.genkidoudou.system.internal.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色-菜单关联表 Mapper。
 */
@Mapper
public interface SysRoleMenuMapper extends BaseBaseMapper<SysRoleMenu> {
}
