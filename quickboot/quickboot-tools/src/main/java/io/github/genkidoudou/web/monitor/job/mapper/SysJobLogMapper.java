package io.github.genkidoudou.web.monitor.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.monitor.job.domain.SysJobLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 调度日志 Mapper。
 */
@Mapper
public interface SysJobLogMapper extends BaseMapper<SysJobLog> {
}
