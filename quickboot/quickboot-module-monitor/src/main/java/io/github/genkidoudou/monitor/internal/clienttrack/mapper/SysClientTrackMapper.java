package io.github.genkidoudou.monitor.internal.clienttrack.mapper;

import io.github.genkidoudou.common.mybatisplus.BaseBaseMapper;
import io.github.genkidoudou.monitor.internal.clienttrack.entity.SysClientTrack;
import org.apache.ibatis.annotations.Mapper;

/**
 * 前端用户行为监控批次 Mapper。
 */
@Mapper
public interface SysClientTrackMapper extends BaseBaseMapper<SysClientTrack> {
}
