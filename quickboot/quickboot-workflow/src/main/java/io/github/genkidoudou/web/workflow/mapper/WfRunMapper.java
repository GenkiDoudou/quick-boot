package io.github.genkidoudou.web.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.workflow.domain.WfRun;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工作流运行实例 Mapper。
 */
@Mapper
public interface WfRunMapper extends BaseMapper<WfRun> {
}
