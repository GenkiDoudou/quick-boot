package io.github.genkidoudou.web.system.config.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.system.config.domain.SysConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统参数 Mapper。
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {
}
