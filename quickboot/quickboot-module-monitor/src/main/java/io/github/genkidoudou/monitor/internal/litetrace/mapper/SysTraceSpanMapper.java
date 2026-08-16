package io.github.genkidoudou.monitor.internal.litetrace.mapper;

import io.github.genkidoudou.common.mybatisplus.BaseBaseMapper;
import io.github.genkidoudou.monitor.internal.litetrace.entity.SysTraceSpan;
import org.apache.ibatis.annotations.Mapper;

/**
 * 链路 span 表 {@code sys_trace_span} 数据访问。
 */
@Mapper
public interface SysTraceSpanMapper extends BaseBaseMapper<SysTraceSpan> {
}
