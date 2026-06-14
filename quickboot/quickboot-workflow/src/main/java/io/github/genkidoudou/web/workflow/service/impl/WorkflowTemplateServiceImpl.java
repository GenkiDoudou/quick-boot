package io.github.genkidoudou.web.workflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.workflow.constants.WfTemplateStatus;
import io.github.genkidoudou.web.workflow.constants.WorkflowConstants;
import io.github.genkidoudou.web.workflow.domain.WfWorkflow;
import io.github.genkidoudou.web.workflow.domain.WfWorkflowTemplate;
import io.github.genkidoudou.web.workflow.domain.WfWorkflowVersion;
import io.github.genkidoudou.web.workflow.dto.WfSaveTemplateGraphBo;
import io.github.genkidoudou.web.workflow.dto.WfTemplateImportBo;
import io.github.genkidoudou.web.workflow.dto.WfTemplateVo;
import io.github.genkidoudou.web.workflow.dto.WfWorkflowTemplateBo;
import io.github.genkidoudou.web.workflow.dto.WfWorkflowTemplateQueryBo;
import io.github.genkidoudou.web.workflow.dto.WfWorkflowTemplateVo;
import io.github.genkidoudou.web.workflow.dto.WorkflowGraphDto;
import io.github.genkidoudou.web.workflow.engine.WorkflowGraphValidator;
import io.github.genkidoudou.web.workflow.mapper.WfWorkflowMapper;
import io.github.genkidoudou.web.workflow.mapper.WfWorkflowTemplateMapper;
import io.github.genkidoudou.web.workflow.mapper.WfWorkflowVersionMapper;
import io.github.genkidoudou.web.workflow.service.WorkflowTemplateService;
import io.github.genkidoudou.web.workflow.template.WorkflowGraphDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作流模板库 CRUD 与图解析实现。
 */
@Service
public class WorkflowTemplateServiceImpl implements WorkflowTemplateService {

    private static final String ADMIN_ROLE_KEY = "admin";

    private final WfWorkflowTemplateMapper templateMapper;
    private final WfWorkflowMapper workflowMapper;
    private final WfWorkflowVersionMapper versionMapper;
    private final WorkflowGraphValidator graphValidator;

    public WorkflowTemplateServiceImpl(WfWorkflowTemplateMapper templateMapper,
                                         WfWorkflowMapper workflowMapper,
                                         WfWorkflowVersionMapper versionMapper,
                                         WorkflowGraphValidator graphValidator) {
        this.templateMapper = templateMapper;
        this.workflowMapper = workflowMapper;
        this.versionMapper = versionMapper;
        this.graphValidator = graphValidator;
    }

    @Override
    public PageInfo<WfWorkflowTemplateVo> page(WfWorkflowTemplateQueryBo query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();
        LambdaQueryWrapper<WfWorkflowTemplate> wrapper = Wrappers.<WfWorkflowTemplate>lambdaQuery()
            .eq(WfWorkflowTemplate::getDeleted, WorkflowConstants.NOT_DELETED)
            .like(StrUtil.isNotBlank(query.getName()), WfWorkflowTemplate::getName, query.getName())
            .like(StrUtil.isNotBlank(query.getCode()), WfWorkflowTemplate::getCode, query.getCode())
            .eq(StrUtil.isNotBlank(query.getStatus()), WfWorkflowTemplate::getStatus, query.getStatus())
            .orderByAsc(WfWorkflowTemplate::getSortOrder)
            .orderByDesc(WfWorkflowTemplate::getUpdateTime);
        Page<WfWorkflowTemplate> mp = templateMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<WfWorkflowTemplateVo> rows = new ArrayList<>();
        for (WfWorkflowTemplate row : mp.getRecords()) {
            rows.add(toListVo(row));
        }
        Page<WfWorkflowTemplateVo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    @Override
    public WfWorkflowTemplateVo getInfo(Long templateId) {
        WfWorkflowTemplate row = requireTemplate(templateId);
        WfWorkflowTemplateVo vo = toListVo(row);
        vo.setGraph(JSONUtil.toBean(row.getGraphJson(), WorkflowGraphDto.class));
        return vo;
    }

    @Override
    public List<WfTemplateVo> listOptions() {
        List<WfWorkflowTemplate> rows = templateMapper.selectList(Wrappers.<WfWorkflowTemplate>lambdaQuery()
            .eq(WfWorkflowTemplate::getDeleted, WorkflowConstants.NOT_DELETED)
            .eq(WfWorkflowTemplate::getStatus, WfTemplateStatus.ENABLED)
            .orderByAsc(WfWorkflowTemplate::getSortOrder)
            .orderByDesc(WfWorkflowTemplate::getUpdateTime));
        List<WfTemplateVo> options = new ArrayList<>(rows.size());
        for (WfWorkflowTemplate row : rows) {
            WfTemplateVo vo = new WfTemplateVo();
            vo.setCode(row.getCode());
            vo.setName(row.getName());
            vo.setDescription(row.getDescription());
            options.add(vo);
        }
        return options;
    }

    @Override
    public WorkflowGraphDto resolveGraphByCode(String code) {
        if (StrUtil.isBlank(code)) {
            return null;
        }
        WfWorkflowTemplate row = templateMapper.selectOne(Wrappers.<WfWorkflowTemplate>lambdaQuery()
            .eq(WfWorkflowTemplate::getCode, code.trim())
            .eq(WfWorkflowTemplate::getDeleted, WorkflowConstants.NOT_DELETED)
            .eq(WfWorkflowTemplate::getStatus, WfTemplateStatus.ENABLED)
            .last("LIMIT 1"));
        if (row == null) {
            return null;
        }
        return JSONUtil.toBean(row.getGraphJson(), WorkflowGraphDto.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(WfWorkflowTemplateBo req) {
        assertCodeUnique(req.getCode(), null);
        WorkflowGraphDto graph = req.getGraph() != null ? req.getGraph() : WorkflowGraphDefaults.minimal();
        validateGraph(graph);

        WfWorkflowTemplate row = new WfWorkflowTemplate();
        row.setCode(req.getCode().trim());
        row.setName(req.getName().trim());
        row.setDescription(StrUtil.nullToEmpty(req.getDescription()));
        row.setGraphJson(JSONUtil.toJsonStr(graph));
        row.setBuiltin(resolveBuiltinForAdd(req.getBuiltin()));
        row.setStatus(normalizeStatus(req.getStatus()));
        row.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
        row.setDeleted(WorkflowConstants.NOT_DELETED);
        templateMapper.insert(row);
        return row.getTemplateId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WfWorkflowTemplateBo req) {
        WfWorkflowTemplate old = requireTemplate(req.getTemplateId());
        if (Integer.valueOf(1).equals(old.getBuiltin()) && StrUtil.isNotBlank(req.getCode())
            && !old.getCode().equals(req.getCode().trim())) {
            throw new WarningException(ErrorCodes.Biz.WORKFLOW_GRAPH_INVALID, "内置模板不可修改编码");
        }
        if (StrUtil.isNotBlank(req.getCode()) && !old.getCode().equals(req.getCode().trim())) {
            assertCodeUnique(req.getCode(), old.getTemplateId());
            old.setCode(req.getCode().trim());
        }
        old.setName(req.getName().trim());
        old.setDescription(StrUtil.nullToEmpty(req.getDescription()));
        if (StrUtil.isNotBlank(req.getStatus())) {
            old.setStatus(normalizeStatus(req.getStatus()));
        }
        if (req.getSortOrder() != null) {
            old.setSortOrder(req.getSortOrder());
        }
        if (req.getGraph() != null) {
            validateGraph(req.getGraph());
            old.setGraphJson(JSONUtil.toJsonStr(req.getGraph()));
        }
        templateMapper.updateById(old);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGraph(WfSaveTemplateGraphBo req) {
        WfWorkflowTemplate row = requireTemplate(req.getTemplateId());
        validateGraph(req.getGraph());
        row.setGraphJson(JSONUtil.toJsonStr(req.getGraph()));
        templateMapper.updateById(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long importFromWorkflow(WfTemplateImportBo req) {
        requireWorkflow(req.getWorkflowId());
        WfWorkflowVersion draft = versionMapper.selectOne(Wrappers.<WfWorkflowVersion>lambdaQuery()
            .eq(WfWorkflowVersion::getWorkflowId, req.getWorkflowId())
            .eq(WfWorkflowVersion::getIsDraft, WorkflowConstants.DRAFT_VERSION)
            .last("LIMIT 1"));
        if (draft == null || StrUtil.isBlank(draft.getGraphJson())) {
            throw new WarningException(ErrorCodes.Biz.WORKFLOW_GRAPH_INVALID, "工作流无草稿图可导入");
        }
        WorkflowGraphDto graph = JSONUtil.toBean(draft.getGraphJson(), WorkflowGraphDto.class);
        validateGraph(graph);

        WfWorkflowTemplateBo meta = req.getTemplate();
        if (meta == null) {
            meta = new WfWorkflowTemplateBo();
        }
        if (StrUtil.isBlank(meta.getCode())) {
            meta.setCode("wf-" + req.getWorkflowId() + "-" + System.currentTimeMillis());
        }
        if (StrUtil.isBlank(meta.getName())) {
            meta.setName("从工作流导入 " + req.getWorkflowId());
        }
        meta.setGraph(graph);
        return add(meta);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatch(List<Long> templateIds) {
        if (templateIds == null || templateIds.isEmpty()) {
            return;
        }
        for (Long id : templateIds) {
            WfWorkflowTemplate row = requireTemplate(id);
            if (Integer.valueOf(1).equals(row.getBuiltin()) && !isCurrentUserAdmin()) {
                throw new WarningException(ErrorCodes.Biz.WORKFLOW_GRAPH_INVALID, "内置模板不可删除: " + row.getCode());
            }
            WfWorkflowTemplate patch = new WfWorkflowTemplate();
            patch.setTemplateId(id);
            patch.setDeleted(WorkflowConstants.DELETED);
            templateMapper.updateById(patch);
        }
    }

    private void requireWorkflow(Long workflowId) {
        WfWorkflow workflow = workflowMapper.selectOne(Wrappers.<WfWorkflow>lambdaQuery()
            .eq(WfWorkflow::getWorkflowId, workflowId)
            .eq(WfWorkflow::getDeleted, WorkflowConstants.NOT_DELETED));
        if (workflow == null) {
            throw new WarningException(ErrorCodes.Biz.WORKFLOW_NOT_FOUND, "工作流不存在");
        }
    }

    private WfWorkflowTemplate requireTemplate(Long templateId) {
        WfWorkflowTemplate row = templateMapper.selectOne(Wrappers.<WfWorkflowTemplate>lambdaQuery()
            .eq(WfWorkflowTemplate::getTemplateId, templateId)
            .eq(WfWorkflowTemplate::getDeleted, WorkflowConstants.NOT_DELETED));
        if (row == null) {
            throw new WarningException(ErrorCodes.Biz.WORKFLOW_TEMPLATE_NOT_FOUND, "工作流模板不存在");
        }
        return row;
    }

    private void assertCodeUnique(String code, Long excludeId) {
        if (StrUtil.isBlank(code)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "模板编码不能为空");
        }
        LambdaQueryWrapper<WfWorkflowTemplate> wrapper = Wrappers.<WfWorkflowTemplate>lambdaQuery()
            .eq(WfWorkflowTemplate::getCode, code.trim())
            .eq(WfWorkflowTemplate::getDeleted, WorkflowConstants.NOT_DELETED);
        if (excludeId != null) {
            wrapper.ne(WfWorkflowTemplate::getTemplateId, excludeId);
        }
        if (templateMapper.selectCount(wrapper) > 0) {
            throw new WarningException(ErrorCodes.Biz.WORKFLOW_GRAPH_INVALID, "模板编码已存在: " + code);
        }
    }

    private void validateGraph(WorkflowGraphDto graph) {
        try {
            graphValidator.validate(graph);
        } catch (IllegalArgumentException ex) {
            throw new WarningException(ErrorCodes.Biz.WORKFLOW_GRAPH_INVALID, ex.getMessage());
        }
    }

    private int resolveBuiltinForAdd(Integer builtin) {
        int value = Integer.valueOf(1).equals(builtin) ? 1 : 0;
        if (value == 1 && !isCurrentUserAdmin()) {
            throw new WarningException(ErrorCodes.Biz.WORKFLOW_GRAPH_INVALID, "仅 admin 角色可创建内置模板");
        }
        return value;
    }

    private boolean isCurrentUserAdmin() {
        try {
            List<String> roles = StpUtil.getRoleList();
            return roles != null && roles.contains(ADMIN_ROLE_KEY);
        } catch (Exception ex) {
            return false;
        }
    }

    private String normalizeStatus(String status) {
        if (WfTemplateStatus.DISABLED.equals(status)) {
            return WfTemplateStatus.DISABLED;
        }
        return WfTemplateStatus.ENABLED;
    }

    private WfWorkflowTemplateVo toListVo(WfWorkflowTemplate row) {
        return BeanUtil.copyProperties(row, WfWorkflowTemplateVo.class);
    }
}
