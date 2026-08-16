package io.github.genkidoudou.system.internal.mapper;

import io.github.genkidoudou.common.mybatisplus.BaseBaseMapper;

import io.github.genkidoudou.system.internal.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜单表 Mapper。
 */
@Mapper
public interface SysMenuMapper extends BaseBaseMapper<SysMenu> {
}
