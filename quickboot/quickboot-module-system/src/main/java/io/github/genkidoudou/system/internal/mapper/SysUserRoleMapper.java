package io.github.genkidoudou.system.internal.mapper;

import io.github.genkidoudou.common.mybatisplus.BaseBaseMapper;

import io.github.genkidoudou.system.internal.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户-角色关联表 Mapper。
 */
@Mapper
public interface SysUserRoleMapper extends BaseBaseMapper<SysUserRole> {
}
