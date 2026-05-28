package io.github.genkidoudou.web.system.operlog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.system.operlog.domain.SysOperLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper。
 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {
}
