package io.github.genkidoudou.web.system.menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.system.menu.domain.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色菜单关联 Mapper。
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {
}
