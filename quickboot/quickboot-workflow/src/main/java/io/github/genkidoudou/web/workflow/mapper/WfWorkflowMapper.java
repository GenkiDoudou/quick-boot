package io.github.genkidoudou.web.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.workflow.domain.WfWorkflow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工作流定义 Mapper。
 */
@Mapper
public interface WfWorkflowMapper extends BaseMapper<WfWorkflow> {
}
