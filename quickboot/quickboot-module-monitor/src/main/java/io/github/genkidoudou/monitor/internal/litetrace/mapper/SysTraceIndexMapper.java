package io.github.genkidoudou.monitor.internal.litetrace.mapper;

import io.github.genkidoudou.common.mybatisplus.BaseBaseMapper;
import io.github.genkidoudou.monitor.internal.litetrace.entity.SysTraceIndex;
import org.apache.ibatis.annotations.Mapper;

/**
 * 链路索引表 {@code sys_trace_index} 数据访问。
 */
@Mapper
public interface SysTraceIndexMapper extends BaseBaseMapper<SysTraceIndex> {
}
