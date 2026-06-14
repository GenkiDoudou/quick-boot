package io.github.genkidoudou.web.workflow.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.web.workflow.dto.WfSaveTemplateGraphBo;
import io.github.genkidoudou.web.workflow.dto.WfTemplateImportBo;
import io.github.genkidoudou.web.workflow.dto.WfTemplateVo;
import io.github.genkidoudou.web.workflow.dto.WfWorkflowTemplateBo;
import io.github.genkidoudou.web.workflow.dto.WfWorkflowTemplateQueryBo;
import io.github.genkidoudou.web.workflow.dto.WfWorkflowTemplateVo;
import io.github.genkidoudou.web.workflow.dto.WorkflowGraphDto;

import java.util.List;

/**
 * 工作流模板库管理服务。
 */
public interface WorkflowTemplateService {

    /**
     * 分页查询模板（管理页）。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageInfo<WfWorkflowTemplateVo> page(WfWorkflowTemplateQueryBo query);

    /**
     * 查询模板详情（含 graph）。
     *
     * @param templateId 模板 ID
     * @return 详情；不存在时返回 null
     */
    WfWorkflowTemplateVo getInfo(Long templateId);

    /**
     * 新建工作流下拉：仅返回启用模板（不含 graph）。
     *
     * @return 选项列表
     */
    List<WfTemplateVo> listOptions();

    /**
     * 按编码解析模板图；不存在时返回 null。
     *
     * @param code 模板编码
     * @return 图 DSL
     */
    WorkflowGraphDto resolveGraphByCode(String code);

    /**
     * 新增模板。
     *
     * @param req 入参
     * @return 新模板 ID
     */
    Long add(WfWorkflowTemplateBo req);

    /**
     * 修改模板元数据（不含 graph，graph 走 saveGraph）。
     *
     * @param req 入参
     */
    void update(WfWorkflowTemplateBo req);

    /**
     * 保存模板图 DSL。
     *
     * @param req 入参
     */
    void saveGraph(WfSaveTemplateGraphBo req);

    /**
     * 从工作流草稿图导入为新模板。
     *
     * @param req 入参
     * @return 新模板 ID
     */
    Long importFromWorkflow(WfTemplateImportBo req);

    /**
     * 批量逻辑删除（内置模板不可删）。
     *
     * @param templateIds 模板 ID 列表
     */
    void removeBatch(List<Long> templateIds);
}
