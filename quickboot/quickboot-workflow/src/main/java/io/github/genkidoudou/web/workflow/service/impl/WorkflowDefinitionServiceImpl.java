package io.github.genkidoudou.web.workflow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.constants.WfWorkflowStatus;
import io.github.genkidoudou.web.workflow.constants.WorkflowConstants;
import io.github.genkidoudou.web.workflow.domain.WfWorkflow;
import io.github.genkidoudou.web.workflow.domain.WfWorkflowVersion;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.dto.WfPublishBo;
import io.github.genkidoudou.web.workflow.dto.WfSaveGraphBo;
import io.github.genkidoudou.web.workflow.dto.WfTemplateVo;
import io.github.genkidoudou.web.workflow.dto.WfWorkflowBo;
import io.github.genkidoudou.web.workflow.dto.WfWorkflowDetailVo;
import io.github.genkidoudou.web.workflow.dto.WfWorkflowQueryBo;
import io.github.genkidoudou.web.workflow.dto.WfWorkflowVo;
import io.github.genkidoudou.web.workflow.dto.WorkflowGraphDto;
import io.github.genkidoudou.web.workflow.engine.WorkflowGraphValidator;
import io.github.genkidoudou.web.workflow.mapper.WfWorkflowMapper;
import io.github.genkidoudou.web.workflow.mapper.WfWorkflowVersionMapper;
import io.github.genkidoudou.web.workflow.service.WorkflowDefinitionService;
import io.github.genkidoudou.web.workflow.service.WorkflowTemplateService;
import io.github.genkidoudou.web.workflow.template.BatchTestWorkflowTemplate;
import io.github.genkidoudou.web.workflow.template.DefaultRagWorkflowTemplate;
import io.github.genkidoudou.web.workflow.template.LoopTestWorkflowTemplate;
import io.github.genkidoudou.web.workflow.template.WorkflowGraphDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作流定义管理服务实现。
 */
@Service
public class WorkflowDefinitionServiceImpl implements WorkflowDefinitionService {

    private final WfWorkflowMapper workflowMapper;
    private final WfWorkflowVersionMapper versionMapper;
    private final WorkflowGraphValidator graphValidator;
    private final WorkflowTemplateService templateService;
    private final DefaultRagWorkflowTemplate defaultRagTemplate;
    private final LoopTestWorkflowTemplate loopTestTemplate;
    private final BatchTestWorkflowTemplate batchTestTemplate;

    public WorkflowDefinitionServiceImpl(WfWorkflowMapper workflowMapper,
                                           WfWorkflowVersionMapper versionMapper,
                                           WorkflowGraphValidator graphValidator,
                                           WorkflowTemplateService templateService,
                                           DefaultRagWorkflowTemplate defaultRagTemplate,
                                           LoopTestWorkflowTemplate loopTestTemplate,
                                           BatchTestWorkflowTemplate batchTestTemplate) {
        this.workflowMapper = workflowMapper;
        this.versionMapper = versionMapper;
        this.graphValidator = graphValidator;
        this.templateService = templateService;
        this.defaultRagTemplate = defaultRagTemplate;
        this.loopTestTemplate = loopTestTemplate;
        this.batchTestTemplate = batchTestTemplate;
    }

    @Override
    public PageInfo<WfWorkflowVo> page(WfWorkflowQueryBo query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();
        LambdaQueryWrapper<WfWorkflow> wrapper = Wrappers.<WfWorkflow>lambdaQuery()
            .eq(WfWorkflow::getDeleted, WorkflowConstants.NOT_DELETED)
            .like(StrUtil.isNotBlank(query.getName()), WfWorkflow::getName, query.getName())
            .eq(StrUtil.isNotBlank(query.getStatus()), WfWorkflow::getStatus, query.getStatus())
            .orderByDesc(WfWorkflow::getCreateTime);
        Page<WfWorkflow> mp = workflowMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<WfWorkflowVo> rows = new ArrayList<>();
        for (WfWorkflow row : mp.getRecords()) {
            rows.add(BeanUtil.copyProperties(row, WfWorkflowVo.class));
        }
        Page<WfWorkflowVo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    @Override
    public WfWorkflowDetailVo getDetail(Long workflowId) {
        WfWorkflow workflow = requireWorkflow(workflowId);
        WfWorkflowDetailVo vo = BeanUtil.copyProperties(workflow, WfWorkflowDetailVo.class);
        WfWorkflowVersion draft = findDraftVersion(workflowId);
        if (draft != null) {
            vo.setDraftVersionId(draft.getVersionId());
            vo.setDraftGraph(JSONUtil.toBean(draft.getGraphJson(), WorkflowGraphDto.class));
        }
        return vo;
    }

    @Override
    public WfWorkflow requireWorkflow(Long workflowId) {
        WfWorkflow workflow = workflowMapper.selectOne(Wrappers.<WfWorkflow>lambdaQuery()
            .eq(WfWorkflow::getWorkflowId, workflowId)
            .eq(WfWorkflow::getDeleted, WorkflowConstants.NOT_DELETED));
        if (workflow == null) {
            throw new WarningException(ErrorCodes.Biz.WORKFLOW_NOT_FOUND, "工作流不存在");
        }
        return workflow;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(WfWorkflowBo req) {
        WfWorkflow row = BeanUtil.copyProperties(req, WfWorkflow.class);
        row.setStatus(WfWorkflowStatus.DRAFT);
        row.setDeleted(WorkflowConstants.NOT_DELETED);
        row.setBotEnabled(0);
        row.setExternalApiEnabled(0);
        workflowMapper.insert(row);

        WorkflowGraphDto initialGraph = resolveTemplateGraph(req.getTemplateCode());
        WfWorkflowVersion draft = newVersion(row.getWorkflowId(), 1, initialGraph, true);
        versionMapper.insert(draft);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WfWorkflowBo req) {
        requireWorkflow(req.getWorkflowId());
        WfWorkflow row = BeanUtil.copyProperties(req, WfWorkflow.class);
        workflowMapper.updateById(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGraph(WfSaveGraphBo req) {
        requireWorkflow(req.getWorkflowId());
        validateGraph(req.getGraph());
        String graphJson = JSONUtil.toJsonStr(req.getGraph());
        WfWorkflowVersion draft = findDraftVersion(req.getWorkflowId());
        if (draft == null) {
            int nextNo = nextVersionNo(req.getWorkflowId());
            draft = newVersion(req.getWorkflowId(), nextNo, req.getGraph(), true);
            versionMapper.insert(draft);
        } else {
            draft.setGraphJson(graphJson);
            draft.setChecksum(DigestUtil.sha256Hex(graphJson));
            versionMapper.updateById(draft);
        }
    }

    @Override
    public void validateGraph(WorkflowGraphDto graph) {
        try {
            graphValidator.validate(graph);
        } catch (IllegalArgumentException ex) {
            throw new WarningException(ErrorCodes.Biz.WORKFLOW_GRAPH_INVALID, ex.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(WfPublishBo req) {
        WfWorkflow workflow = requireWorkflow(req.getWorkflowId());
        WfWorkflowVersion draft = findDraftVersion(req.getWorkflowId());
        if (draft == null) {
            throw new WarningException(ErrorCodes.Biz.WORKFLOW_GRAPH_INVALID, "无草稿版本可发布");
        }
        WorkflowGraphDto graph = JSONUtil.toBean(draft.getGraphJson(), WorkflowGraphDto.class);
        validateGraph(graph);

        draft.setIsDraft(WorkflowConstants.PUBLISHED_VERSION);
        versionMapper.updateById(draft);

        WfWorkflowVersion newDraft = newVersion(req.getWorkflowId(), draft.getVersionNo() + 1, graph, true);
        versionMapper.insert(newDraft);

        workflow.setPublishedVersionId(draft.getVersionId());
        workflow.setStatus(WfWorkflowStatus.PUBLISHED);
        workflowMapper.updateById(workflow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatch(List<Long> workflowIds) {
        if (workflowIds == null || workflowIds.isEmpty()) {
            return;
        }
        for (Long id : workflowIds) {
            WfWorkflow row = new WfWorkflow();
            row.setWorkflowId(id);
            row.setDeleted(WorkflowConstants.DELETED);
            workflowMapper.updateById(row);
        }
    }

    @Override
    public List<WfTemplateVo> listTemplates() {
        List<WfTemplateVo> options = templateService.listOptions();
        if (!options.isEmpty()) {
            return options;
        }
        return List.of(defaultRagTemplate.build(), loopTestTemplate.build(), batchTestTemplate.build());
    }

    private WorkflowGraphDto resolveTemplateGraph(String templateCode) {
        if (StrUtil.isBlank(templateCode)) {
            return WorkflowGraphDefaults.minimal();
        }
        WorkflowGraphDto graph = templateService.resolveGraphByCode(templateCode);
        if (graph != null) {
            return graph;
        }
        if (WorkflowConstants.TEMPLATE_DEFAULT_RAG.equals(templateCode)) {
            return defaultRagTemplate.build().getGraph();
        }
        if (WorkflowConstants.TEMPLATE_LOOP_COUNT_TEST.equals(templateCode)) {
            return loopTestTemplate.build().getGraph();
        }
        if (WorkflowConstants.TEMPLATE_BATCH_ARRAY_TEST.equals(templateCode)) {
            return batchTestTemplate.build().getGraph();
        }
        throw new WarningException(ErrorCodes.Biz.WORKFLOW_GRAPH_INVALID, "未知模板编码: " + templateCode);
    }

    /**
     * 获取运行用版本 graph JSON。
     *
     * @param workflowId    工作流 ID
     * @param useDraft      是否使用草稿
     * @param usePublished  是否使用发布版本（与 useDraft 互斥时 publish 优先）
     * @return 版本实体
     */
    public WfWorkflowVersion resolveRunVersion(Long workflowId, boolean useDraft, boolean usePublished) {
        if (useDraft) {
            WfWorkflowVersion draft = findDraftVersion(workflowId);
            if (draft != null) {
                return draft;
            }
        }
        WfWorkflow workflow = requireWorkflow(workflowId);
        if (workflow.getPublishedVersionId() != null) {
            WfWorkflowVersion published = versionMapper.selectById(workflow.getPublishedVersionId());
            if (published != null) {
                return published;
            }
        }
        WfWorkflowVersion draft = findDraftVersion(workflowId);
        if (draft != null) {
            return draft;
        }
        throw new WarningException(ErrorCodes.Biz.WORKFLOW_NOT_FOUND, "无可运行的版本");
    }

    private WfWorkflowVersion findDraftVersion(Long workflowId) {
        return versionMapper.selectOne(Wrappers.<WfWorkflowVersion>lambdaQuery()
            .eq(WfWorkflowVersion::getWorkflowId, workflowId)
            .eq(WfWorkflowVersion::getIsDraft, WorkflowConstants.DRAFT_VERSION)
            .last("LIMIT 1"));
    }

    private int nextVersionNo(Long workflowId) {
        WfWorkflowVersion latest = versionMapper.selectOne(Wrappers.<WfWorkflowVersion>lambdaQuery()
            .eq(WfWorkflowVersion::getWorkflowId, workflowId)
            .orderByDesc(WfWorkflowVersion::getVersionNo)
            .last("LIMIT 1"));
        return latest == null ? 1 : latest.getVersionNo() + 1;
    }

    private WfWorkflowVersion newVersion(Long workflowId, int versionNo, WorkflowGraphDto graph, boolean draft) {
        String graphJson = JSONUtil.toJsonStr(graph);
        WfWorkflowVersion version = new WfWorkflowVersion();
        version.setWorkflowId(workflowId);
        version.setVersionNo(versionNo);
        version.setGraphJson(graphJson);
        version.setChecksum(DigestUtil.sha256Hex(graphJson));
        version.setIsDraft(draft ? WorkflowConstants.DRAFT_VERSION : WorkflowConstants.PUBLISHED_VERSION);
        return version;
    }

    /**
     * 从 graph 解析 start 节点 ID。
     */
    public static String findStartNodeId(WorkflowGraphDto graph) {
        for (GraphNodeDto node : graph.getNodes()) {
            if (WfNodeType.START.equals(node.getType())) {
                return node.getId();
            }
        }
        throw new WarningException(ErrorCodes.Biz.WORKFLOW_GRAPH_INVALID, "缺少 start 节点");
    }
}
