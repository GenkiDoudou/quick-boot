package io.github.genkidoudou.web.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.workflow.domain.WfRunStep;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工作流运行步骤 Mapper。
 */
@Mapper
public interface WfRunStepMapper extends BaseMapper<WfRunStep> {
}
