package io.github.genkidoudou.web.workflow.support;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.web.workflow.constants.WfTemplateStatus;
import io.github.genkidoudou.web.workflow.constants.WorkflowConstants;
import io.github.genkidoudou.web.workflow.domain.WfWorkflowTemplate;
import io.github.genkidoudou.web.workflow.dto.WfTemplateVo;
import io.github.genkidoudou.web.workflow.mapper.WfWorkflowTemplateMapper;
import io.github.genkidoudou.web.workflow.template.BatchTestWorkflowTemplate;
import io.github.genkidoudou.web.workflow.template.DefaultRagWorkflowTemplate;
import io.github.genkidoudou.web.workflow.template.LoopTestWorkflowTemplate;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 首次启动时将 Java 内置模板写入 {@code wf_workflow_template}（表为空时执行）。
 */
@Component
@Order(90)
public class WorkflowTemplateSeeder implements ApplicationRunner {

    private final WfWorkflowTemplateMapper templateMapper;
    private final DefaultRagWorkflowTemplate defaultRagTemplate;
    private final LoopTestWorkflowTemplate loopTestTemplate;
    private final BatchTestWorkflowTemplate batchTestTemplate;

    public WorkflowTemplateSeeder(WfWorkflowTemplateMapper templateMapper,
                                  DefaultRagWorkflowTemplate defaultRagTemplate,
                                  LoopTestWorkflowTemplate loopTestTemplate,
                                  BatchTestWorkflowTemplate batchTestTemplate) {
        this.templateMapper = templateMapper;
        this.defaultRagTemplate = defaultRagTemplate;
        this.loopTestTemplate = loopTestTemplate;
        this.batchTestTemplate = batchTestTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        Long count = templateMapper.selectCount(Wrappers.<WfWorkflowTemplate>lambdaQuery()
            .eq(WfWorkflowTemplate::getDeleted, WorkflowConstants.NOT_DELETED));
        if (count != null && count > 0) {
            return;
        }
        seedBuiltin(defaultRagTemplate.build(), 10);
        seedBuiltin(loopTestTemplate.build(), 20);
        seedBuiltin(batchTestTemplate.build(), 30);
    }

    private void seedBuiltin(WfTemplateVo template, int sortOrder) {
        WfWorkflowTemplate row = new WfWorkflowTemplate();
        row.setCode(template.getCode());
        row.setName(template.getName());
        row.setDescription(StrUtil.nullToEmpty(template.getDescription()));
        row.setGraphJson(JSONUtil.toJsonStr(template.getGraph()));
        row.setBuiltin(1);
        row.setStatus(WfTemplateStatus.ENABLED);
        row.setSortOrder(sortOrder);
        row.setDeleted(WorkflowConstants.NOT_DELETED);
        templateMapper.insert(row);
    }
}
