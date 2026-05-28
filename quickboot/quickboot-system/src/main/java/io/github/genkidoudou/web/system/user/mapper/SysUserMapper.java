package io.github.genkidoudou.web.system.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.system.user.domain.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户 Mapper。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
