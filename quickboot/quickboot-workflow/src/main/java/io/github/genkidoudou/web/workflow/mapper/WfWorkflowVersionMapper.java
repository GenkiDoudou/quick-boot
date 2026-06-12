package io.github.genkidoudou.web.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.workflow.domain.WfWorkflowVersion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工作流版本 Mapper。
 */
@Mapper
public interface WfWorkflowVersionMapper extends BaseMapper<WfWorkflowVersion> {
}
