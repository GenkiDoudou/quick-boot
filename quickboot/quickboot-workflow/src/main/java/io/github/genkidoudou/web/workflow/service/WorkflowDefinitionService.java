package io.github.genkidoudou.web.workflow.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.web.workflow.domain.WfWorkflow;
import io.github.genkidoudou.web.workflow.dto.WfPublishBo;
import io.github.genkidoudou.web.workflow.dto.WfSaveGraphBo;
import io.github.genkidoudou.web.workflow.dto.WfTemplateVo;
import io.github.genkidoudou.web.workflow.dto.WfValidateGraphBo;
import io.github.genkidoudou.web.workflow.dto.WfWorkflowBo;
import io.github.genkidoudou.web.workflow.dto.WfWorkflowDetailVo;
import io.github.genkidoudou.web.workflow.dto.WfWorkflowQueryBo;
import io.github.genkidoudou.web.workflow.dto.WfWorkflowVo;
import io.github.genkidoudou.web.workflow.dto.WorkflowGraphDto;

import java.util.List;

/**
 * 工作流定义管理服务。
 */
public interface WorkflowDefinitionService {

    PageInfo<WfWorkflowVo> page(WfWorkflowQueryBo query);

    WfWorkflowDetailVo getDetail(Long workflowId);

    WfWorkflow requireWorkflow(Long workflowId);

    void add(WfWorkflowBo req);

    void update(WfWorkflowBo req);

    void saveGraph(WfSaveGraphBo req);

    void validateGraph(WorkflowGraphDto graph);

    void publish(WfPublishBo req);

    void removeBatch(List<Long> workflowIds);

    List<WfTemplateVo> listTemplates();
}
