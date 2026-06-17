package io.github.genkidoudou.web.aiapp.support;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.aiapp.constants.AiAppType;
import io.github.genkidoudou.web.aiapp.domain.AiApp;
import io.github.genkidoudou.web.aiapp.dto.AgentAppConfigDto;
import io.github.genkidoudou.web.aiapp.dto.WorkflowAppConfigDto;
import org.springframework.stereotype.Component;

/**
 * AI 应用 config_json 校验器。
 */
@Component
public class AiAppConfigValidator {

    /**
     * 校验应用草稿配置是否满足类型要求（发布前调用）。
     *
     * @param app 应用实体（含 appType 与 configJson）
     */
    public void validate(AiApp app) {
        if (app == null || StrUtil.isBlank(app.getAppType())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "应用类型不能为空");
        }
        if (StrUtil.isBlank(app.getConfigJson())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "应用配置不能为空");
        }
        if (AiAppType.AGENT.equals(app.getAppType())) {
            validateAgent(app.getConfigJson());
        } else if (AiAppType.WORKFLOW.equals(app.getAppType())) {
            validateWorkflow(app.getConfigJson());
        } else {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "不支持的应用类型: " + app.getAppType());
        }
    }

    /**
     * 校验智能体配置：必须包含 chatModelId。
     *
     * @param configJson 配置 JSON
     */
    public void validateAgent(String configJson) {
        AgentAppConfigDto config = JSONUtil.toBean(configJson, AgentAppConfigDto.class);
        if (config.getChatModelId() == null || config.getChatModelId() < 1) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "智能体应用必须配置 chatModelId");
        }
    }

    /**
     * 校验高级编排配置：必须包含 workflowId。
     *
     * @param configJson 配置 JSON
     */
    public void validateWorkflow(String configJson) {
        WorkflowAppConfigDto config = JSONUtil.toBean(configJson, WorkflowAppConfigDto.class);
        if (config.getWorkflowId() == null || config.getWorkflowId() < 1) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "高级编排应用必须配置 workflowId");
        }
    }
}
