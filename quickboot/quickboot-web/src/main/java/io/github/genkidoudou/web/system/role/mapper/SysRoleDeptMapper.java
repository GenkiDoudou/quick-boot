package io.github.genkidoudou.web.system.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.system.role.domain.SysRoleDept;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色-部门（数据权限）Mapper。
 */
@Mapper
public interface SysRoleDeptMapper extends BaseMapper<SysRoleDept> {
}
