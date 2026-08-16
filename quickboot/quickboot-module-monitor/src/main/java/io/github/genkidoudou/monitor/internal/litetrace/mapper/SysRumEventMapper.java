package io.github.genkidoudou.monitor.internal.litetrace.mapper;

import io.github.genkidoudou.common.mybatisplus.BaseBaseMapper;
import io.github.genkidoudou.monitor.internal.litetrace.entity.SysRumEvent;
import org.apache.ibatis.annotations.Mapper;

/**
 * RUM 原始事件表 {@code sys_rum_event} 数据访问。
 */
@Mapper
public interface SysRumEventMapper extends BaseBaseMapper<SysRumEvent> {
}
