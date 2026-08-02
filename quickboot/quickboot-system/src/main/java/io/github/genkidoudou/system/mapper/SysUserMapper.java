package io.github.genkidoudou.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户 Mapper。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
