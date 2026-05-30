package io.github.genkidoudou.web.monitor.clienttrack.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.monitor.clienttrack.domain.SysClientTrack;
import org.apache.ibatis.annotations.Mapper;

/**
 * 前端用户行为监控批次 Mapper。
 */
@Mapper
public interface SysClientTrackMapper extends BaseMapper<SysClientTrack> {
}
