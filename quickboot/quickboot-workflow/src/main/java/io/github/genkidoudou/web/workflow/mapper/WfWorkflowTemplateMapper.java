package io.github.genkidoudou.web.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.workflow.domain.WfWorkflowTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工作流模板 Mapper。
 */
@Mapper
public interface WfWorkflowTemplateMapper extends BaseMapper<WfWorkflowTemplate> {
}
