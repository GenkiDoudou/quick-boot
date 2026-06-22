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
     * @param bindings  工作流 Tool 绑定
     * @param kbIds     智能体绑定的知识库（注入工作流 sys.kbId，取首个）
     * @param useDraft  是否使用草稿版本（编排预览 true，已发布演示 false）
     * @return ToolCallback 数组
     */
    public ToolCallback[] create(List<AgentAppConfigDto.WorkflowBindingDto> bindings,
                                 List<Long> kbIds,
                                 boolean useDraft) {
        if (bindings == null || bindings.isEmpty()) {
            return new ToolCallback[0];
        }
        Long kbId = resolveKbId(kbIds);
        List<ToolCallback> callbacks = new ArrayList<>();
        for (AgentAppConfigDto.WorkflowBindingDto binding : bindings) {
            if (binding == null || binding.getWorkflowId() == null || StrUtil.isBlank(binding.getToolName())) {
                continue;
            }
            callbacks.add(createOne(binding, kbId, useDraft));
        }
        return callbacks.toArray(new ToolCallback[0]);
    }

    private ToolCallback createOne(AgentAppConfigDto.WorkflowBindingDto binding, Long kbId, boolean useDraft) {
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
                    bo.setUseDraft(useDraft);
                    bo.setStream(false);
                    bo.setKbId(kbId);
                    bo.setInputs(buildWorkflowInputs(query));
                    WfRunDetailVo result = workflowRunService.debugRun(bo);
                    return formatWorkflowOutput(result);
                } catch (Exception ex) {
                    return "工作流执行失败: " + ex.getMessage();
                }
            }
        };
    }

    /**
     * 映射 start 节点常见入参名：设计器里常用 question，Tool 约定为 query。
     */
    private Map<String, Object> buildWorkflowInputs(String query) {
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("query", query);
        inputs.put("question", query);
        inputs.put("message", query);
        return inputs;
    }

    private String formatWorkflowOutput(WfRunDetailVo result) {
        if (result.getOutputs() == null || result.getOutputs().isEmpty()) {
            return "工作流执行完成，但未产生输出";
        }
        Map<String, Object> outputs = result.getOutputs();
        for (String key : List.of("text", "answer", "output", "result")) {
            Object value = outputs.get(key);
            if (value != null && StrUtil.isNotBlank(String.valueOf(value))) {
                return String.valueOf(value);
            }
        }
        return JSONUtil.toJsonStr(outputs);
    }

    private Long resolveKbId(List<Long> kbIds) {
        if (kbIds == null || kbIds.isEmpty()) {
            return null;
        }
        for (Long kbId : kbIds) {
            if (kbId != null) {
                return kbId;
            }
        }
        return null;
    }

    private String parseQuery(String toolInput) {
        if (StrUtil.isBlank(toolInput)) {
            return "";
        }
        try {
            Map<?, ?> map = JSONUtil.toBean(toolInput, Map.class);
            Object query = map.get("query");
            if (query != null && StrUtil.isNotBlank(String.valueOf(query))) {
                return String.valueOf(query);
            }
            Object question = map.get("question");
            if (question != null && StrUtil.isNotBlank(String.valueOf(question))) {
                return String.valueOf(question);
            }
            return toolInput;
        } catch (Exception ex) {
            return toolInput;
        }
    }
}
