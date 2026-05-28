package io.github.genkidoudou.web.system.menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.system.menu.domain.SysMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜单表 Mapper。
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
}
