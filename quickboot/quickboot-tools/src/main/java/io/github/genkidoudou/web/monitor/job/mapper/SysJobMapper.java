package io.github.genkidoudou.web.monitor.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.monitor.job.domain.SysJob;
import org.apache.ibatis.annotations.Mapper;

/**
 * 定时任务 Mapper。
 */
@Mapper
public interface SysJobMapper extends BaseMapper<SysJob> {
}
