package io.github.genkidoudou.web.aiapp.tool;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.aiapp.dto.AgentAppConfigDto;
import io.github.genkidoudou.web.workflow.dto.WfRunDebugBo;
import io.github.genkidoudou.web.workflow.dto.WfRunDetailVo;
import io.github.genkidoudou.web.workflow.service.WorkflowRunService;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作流 Tool 工厂：按 workflowBindings 为每个绑定创建 ToolCallback。
 */
@Component
public class WorkflowToolFactory {

    private static final String INPUT_SCHEMA = """
        {
          "type": "object",
          "properties": {
            "query": { "type": "string", "description": "用户问题或任务描述" }
          },
          "required": ["query"]
        }
        """;

    private final WorkflowRunService workflowRunService;

    public WorkflowToolFactory(WorkflowRunService workflowRunService) {
        this.workflowRunService = workflowRunService;
    }

    /**
     * 根据工作流绑定列表创建 ToolCallback 数组。
     *
     * @param bindings 工作流 Tool 绑定
     * @return ToolCallback 数组
     */
    public ToolCallback[] create(List<AgentAppConfigDto.WorkflowBindingDto> bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return new ToolCallback[0];
        }
        List<ToolCallback> callbacks = new ArrayList<>();
        for (AgentAppConfigDto.WorkflowBindingDto binding : bindings) {
            if (binding == null || binding.getWorkflowId() == null || StrUtil.isBlank(binding.getToolName())) {
                continue;
            }
            callbacks.add(createOne(binding));
        }
        return callbacks.toArray(new ToolCallback[0]);
    }

    private ToolCallback createOne(AgentAppConfigDto.WorkflowBindingDto binding) {
        ToolDefinition definition = ToolDefinition.builder()
            .name(binding.getToolName())
            .description(StrUtil.blankToDefault(binding.getDescription(), binding.getToolName()))
            .inputSchema(INPUT_SCHEMA)
            .build();
        return new ToolCallback() {
            @Override
            public ToolDefinition getToolDefinition() {
                return definition;
            }

            @Override
            public String call(String toolInput) {
                String query = parseQuery(toolInput);
                if (StrUtil.isBlank(query)) {
                    return "query 不能为空";
                }
                try {
                    WfRunDebugBo bo = new WfRunDebugBo();
                    bo.setWorkflowId(binding.getWorkflowId());
                    bo.setUseDraft(false);
                    bo.setStream(false);
                    Map<String, Object> inputs = new HashMap<>();
                    inputs.put("query", query);
                    bo.setInputs(inputs);
                    WfRunDetailVo result = workflowRunService.debugRun(bo);
                    if (result.getOutputs() != null && !result.getOutputs().isEmpty()) {
                        Object answer = result.getOutputs().get("answer");
                        if (answer != null) {
                            return String.valueOf(answer);
                        }
                        Object text = result.getOutputs().get("text");
                        if (text != null) {
                            return String.valueOf(text);
                        }
                        return JSONUtil.toJsonStr(result.getOutputs());
                    }
                    return "工作流执行完成，但未产生输出";
                } catch (Exception ex) {
                    return "工作流执行失败: " + ex.getMessage();
                }
            }
        };
    }

    private String parseQuery(String toolInput) {
        if (StrUtil.isBlank(toolInput)) {
            return "";
        }
        try {
            Map<?, ?> map = JSONUtil.toBean(toolInput, Map.class);
            Object query = map.get("query");
            return query == null ? toolInput : String.valueOf(query);
        } catch (Exception ex) {
            return toolInput;
        }
    }
}
