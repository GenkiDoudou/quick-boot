package io.github.genkidoudou.quartz.internal.mapper;

import io.github.genkidoudou.common.mybatisplus.BaseBaseMapper;
import io.github.genkidoudou.quartz.internal.entity.SysJobLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 调度日志 Mapper。
 */
@Mapper
public interface SysJobLogMapper extends BaseBaseMapper<SysJobLog> {
}
