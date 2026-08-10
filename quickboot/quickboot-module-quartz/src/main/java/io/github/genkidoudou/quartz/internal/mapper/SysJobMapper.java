package io.github.genkidoudou.quartz.internal.mapper;

import io.github.genkidoudou.common.mybatisplus.BaseBaseMapper;
import io.github.genkidoudou.quartz.internal.entity.SysJob;
import org.apache.ibatis.annotations.Mapper;

/**
 * 定时任务 Mapper。
 */
@Mapper
public interface SysJobMapper extends BaseBaseMapper<SysJob> {
}
