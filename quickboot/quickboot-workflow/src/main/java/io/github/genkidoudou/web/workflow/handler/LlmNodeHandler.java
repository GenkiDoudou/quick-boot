package io.github.genkidoudou.web.workflow.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.knowledge.config.KnowledgeProperties;
import io.github.genkidoudou.web.knowledge.mcp.runtime.McpToolInvocationRecord;
import io.github.genkidoudou.web.knowledge.mcp.runtime.McpToolCallbackProvider;
import io.github.genkidoudou.web.knowledge.mcp.runtime.McpToolUsageTracker;
import io.github.genkidoudou.web.knowledge.mcp.runtime.McpTrackingToolCallbacks;
import io.github.genkidoudou.web.workflow.constants.WfNodeType;
import io.github.genkidoudou.web.workflow.dto.GraphNodeDto;
import io.github.genkidoudou.web.workflow.engine.InputParameterTemplateRenderer;
import io.github.genkidoudou.web.workflow.engine.NodeHandler;
import io.github.genkidoudou.web.workflow.engine.NodeResult;
import io.github.genkidoudou.web.workflow.engine.WorkflowContext;
import io.github.genkidoudou.web.workflow.stream.WorkflowStreamEmitter;
import io.github.genkidoudou.web.workflow.support.WorkflowAiGuard;
import io.github.genkidoudou.web.workflow.util.JsonDeepParseUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 大模型节点：支持模型选择、输入参数、系统/用户提示词、输出格式（text / markdown / json）及 MCP 工具调用。
 * <p>
 * json 模式：将模型回复解析为 JSON 并按 outputVariables 展开字段；text/markdown 模式：作为纯文本写入 output/text。
 * 启用 MCP 时挂载所选 MCP 服务的 Tool Callbacks，并在输出中写入 {@code mcpToolsUsed} 与 {@code mcpToolResults}。
 */
@Component
public class LlmNodeHandler implements NodeHandler {

    private static final Pattern JSON_FENCE = Pattern.compile("^```(?:json)?\\s*([\\s\\S]*?)\\s*```$", Pattern.CASE_INSENSITIVE);

    private static final String MCP_TOOL_HINT = """

        若已提供外部 MCP 工具且与问题相关，可调用工具获取实时数据；不得捏造引用或工具结果。
        """;

    private final WorkflowAiGuard aiGuard;
    private final InputParameterTemplateRenderer inputParameterRenderer;
    private final WorkflowStreamEmitter streamEmitter;
    private final ObjectProvider<KnowledgeProperties> knowledgeProperties;
    private final ObjectProvider<McpToolCallbackProvider> mcpToolCallbackProvider;
    private final ObjectProvider<McpToolUsageTracker> mcpToolUsageTracker;

    public LlmNodeHandler(WorkflowAiGuard aiGuard,
                          InputParameterTemplateRenderer inputParameterRenderer,
                          WorkflowStreamEmitter streamEmitter,
                          ObjectProvider<KnowledgeProperties> knowledgeProperties,
                          ObjectProvider<McpToolCallbackProvider> mcpToolCallbackProvider,
                          ObjectProvider<McpToolUsageTracker> mcpToolUsageTracker) {
        this.aiGuard = aiGuard;
        this.inputParameterRenderer = inputParameterRenderer;
        this.streamEmitter = streamEmitter;
        this.knowledgeProperties = knowledgeProperties;
        this.mcpToolCallbackProvider = mcpToolCallbackProvider;
        this.mcpToolUsageTracker = mcpToolUsageTracker;
    }

    @Override
    public String type() {
        return WfNodeType.LLM;
    }

    @Override
    @SuppressWarnings("unchecked")
    public NodeResult execute(GraphNodeDto node, WorkflowContext context) {
        Map<String, Object> data = node.getData() == null ? Map.of() : node.getData();
        Long nodeModelId = parseLong(data.get("chatModelId"));
        ChatModel chatModel = aiGuard.requireChatModelInstance(workflowId(context), nodeModelId);

        String outputFormat = String.valueOf(data.getOrDefault("outputFormat", "text"));
        Map<String, Object> inputLocals = inputParameterRenderer.resolveInputVariables(data.get("inputVariables"), context);
        String systemPrompt = augmentSystemPrompt(
            inputParameterRenderer.render(renderRaw(data.get("systemPrompt")), inputLocals),
            outputFormat,
            data.get("outputVariables")
        );
        String userPrompt = inputParameterRenderer.render(renderRaw(data.get("userPrompt")), inputLocals);

        if (StrUtil.isBlank(userPrompt)) {
            return NodeResult.failed("用户提示词为空，请配置提示词并在输入参数中声明所引用的 {{参数名}}");
        }

        double temperature = parseTemperature(data.get("temperature"));
        List<Long> mcpIds = parseMcpIds(data.get("mcpIds"));
        boolean wantMcpTools = Boolean.TRUE.equals(data.get("useMcpTools")) && !mcpIds.isEmpty();

        McpToolUsageTracker tracker = mcpToolUsageTracker.getIfAvailable();
        if (tracker != null) {
            tracker.clear();
        }

        ChatClient.Builder clientBuilder = ChatClient.builder(chatModel);
        boolean mcpActive = false;
        if (wantMcpTools) {
            KnowledgeProperties properties = knowledgeProperties.getIfAvailable();
            if (properties != null && properties.getMcp().isEnabled()) {
                McpToolCallbackProvider toolProvider = mcpToolCallbackProvider.getIfAvailable();
                if (toolProvider != null && tracker != null) {
                    ToolCallback[] callbacks = McpTrackingToolCallbacks.wrap(
                        toolProvider.getToolCallbacks(mcpIds), tracker);
                    if (callbacks.length > 0) {
                        systemPrompt = systemPrompt + MCP_TOOL_HINT;
                        clientBuilder.defaultToolCallbacks(callbacks);
                        mcpActive = true;
                    }
                }
            }
        }

        boolean streaming = Boolean.TRUE.equals(data.get("streaming"))
            && context.isStreamEnabled()
            && !mcpActive;

        try {
            ChatClient client = clientBuilder.defaultSystem(systemPrompt).build();
            String text;
            if (streaming) {
                text = executeStreaming(node.getId(), context, client, userPrompt, temperature);
            } else {
                text = client.prompt()
                    .options(buildChatOptions(temperature))
                    .user(userPrompt)
                    .call()
                    .content();
            }
            if (text == null) {
                text = "";
            }
            List<McpToolInvocationRecord> mcpInvocations = tracker != null ? tracker.drainInvocations() : List.of();
            return NodeResult.success(buildOutputs(text, data, mcpInvocations));
        } catch (Exception ex) {
            if (tracker != null) {
                tracker.clear();
            }
            return NodeResult.failed("大模型调用失败: " + ex.getMessage());
        }
    }

    private String executeStreaming(String nodeId, WorkflowContext context, ChatClient client,
                                    String userPrompt, double temperature) {
        StringBuilder accumulated = new StringBuilder();
        client.prompt()
            .options(buildChatOptions(temperature))
            .user(userPrompt)
            .stream()
            .content()
            .doOnNext(delta -> {
                if (delta != null && !delta.isEmpty()) {
                    accumulated.append(delta);
                    streamEmitter.emitLlmDelta(context.getRunId(), nodeId, delta, accumulated.toString());
                }
            })
            .blockLast();
        return accumulated.toString();
    }

    private String renderRaw(Object template) {
        return template == null ? "" : String.valueOf(template);
    }

    @SuppressWarnings("unchecked")
    private String augmentSystemPrompt(String systemPrompt, String outputFormat, Object outputVariablesObj) {
        String base = systemPrompt == null ? "" : systemPrompt;
        if ("json".equals(outputFormat)) {
            StringBuilder sb = new StringBuilder(base);
            sb.append("\n\n你必须仅输出合法 JSON，不要包含 Markdown 代码块或其它说明文字。");
            if (outputVariablesObj instanceof List<?> list && !list.isEmpty()) {
                sb.append("\nJSON 对象须包含以下字段：");
                for (Object item : list) {
                    if (!(item instanceof Map<?, ?> row)) {
                        continue;
                    }
                    String key = row.get("key") == null ? "" : String.valueOf(row.get("key")).trim();
                    if (StrUtil.isNotBlank(key)) {
                        sb.append("\n- ").append(key);
                        Object desc = row.get("description");
                        if (desc != null && StrUtil.isNotBlank(String.valueOf(desc))) {
                            sb.append("（").append(desc).append('）');
                        }
                    }
                }
            }
            return sb.toString();
        }
        if ("markdown".equals(outputFormat)) {
            return base + "\n\n请使用 Markdown 格式组织回答。";
        }
        return base;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildOutputs(String text, Map<String, Object> data,
                                             List<McpToolInvocationRecord> mcpInvocations) {
        Map<String, Object> outputs = new HashMap<>();
        String outputFormat = String.valueOf(data.getOrDefault("outputFormat", "text"));
        outputs.put("text", text);
        if (mcpInvocations != null && !mcpInvocations.isEmpty()) {
            outputs.put("mcpToolsUsed", mcpInvocations.stream().map(McpToolInvocationRecord::toolName).toList());
            outputs.put("mcpToolResults", toMcpToolResultMaps(mcpInvocations));
        }

        if ("json".equals(outputFormat)) {
            Object parsed = parseLlmJson(text);
            if (parsed instanceof Map<?, ?> map) {
                Map<String, Object> jsonMap = new LinkedHashMap<>();
                map.forEach((k, v) -> jsonMap.put(String.valueOf(k), JsonDeepParseUtil.deepParse(v)));
                outputs.put("json", jsonMap);
                applyConfiguredOutputVariables(outputs, jsonMap, data.get("outputVariables"));
            } else if (parsed instanceof List<?> list) {
                outputs.put("json", list);
            }
            return outputs;
        }

        String primaryKey = resolvePrimaryOutputKey(data.get("outputVariables"), "output");
        outputs.put(primaryKey, text);
        return outputs;
    }

    /**
     * 将 MCP 调用记录转为工作流节点输出结构（toolName / input / output）。
     */
    private List<Map<String, Object>> toMcpToolResultMaps(List<McpToolInvocationRecord> invocations) {
        List<Map<String, Object>> rows = new ArrayList<>(invocations.size());
        for (McpToolInvocationRecord inv : invocations) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("toolName", inv.toolName());
            row.put("input", parseToolPayload(inv.input()));
            row.put("output", parseToolPayload(inv.output()));
            rows.add(row);
        }
        return rows;
    }

    /**
     * 尝试将工具入参/返回解析为 JSON 对象或数组，否则保留原始字符串。
     */
    private Object parseToolPayload(String raw) {
        if (StrUtil.isBlank(raw)) {
            return "";
        }
        String trimmed = raw.trim();
        if (JSONUtil.isTypeJSONObject(trimmed)) {
            return JsonDeepParseUtil.deepParse(JSONUtil.parseObj(trimmed));
        }
        if (JSONUtil.isTypeJSONArray(trimmed)) {
            return JsonDeepParseUtil.deepParse(JSONUtil.parseArray(trimmed));
        }
        return raw;
    }

    @SuppressWarnings("unchecked")
    private void applyConfiguredOutputVariables(Map<String, Object> outputs, Map<String, Object> parsed,
                                              Object outputVariablesObj) {
        if (!(outputVariablesObj instanceof List<?> list) || list.isEmpty()) {
            parsed.forEach((k, v) -> {
                if (!outputs.containsKey(k)) {
                    outputs.put(k, v);
                }
            });
            return;
        }
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> row)) {
                continue;
            }
            String key = row.get("key") == null ? "" : String.valueOf(row.get("key")).trim();
            if (StrUtil.isBlank(key)) {
                continue;
            }
            if (parsed.containsKey(key)) {
                outputs.put(key, parsed.get(key));
            }
        }
    }

    private String resolvePrimaryOutputKey(Object outputVariablesObj, String defaultKey) {
        if (outputVariablesObj instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof Map<?, ?> row && row.get("key") != null) {
                String key = String.valueOf(row.get("key")).trim();
                if (StrUtil.isNotBlank(key)) {
                    return key;
                }
            }
        }
        return defaultKey;
    }

    private Object parseLlmJson(String text) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        String candidate = stripJsonFence(text.trim());
        if (JSONUtil.isTypeJSONObject(candidate)) {
            return JSONUtil.parseObj(candidate);
        }
        if (JSONUtil.isTypeJSONArray(candidate)) {
            return JSONUtil.parseArray(candidate);
        }
        return null;
    }

    private String stripJsonFence(String text) {
        Matcher matcher = JSON_FENCE.matcher(text);
        if (matcher.matches()) {
            return matcher.group(1).trim();
        }
        return text;
    }

    private ChatOptions buildChatOptions(double temperature) {
        return ChatOptions.builder().temperature(temperature).build();
    }

    private double parseTemperature(Object raw) {
        if (raw instanceof Number number) {
            return number.doubleValue();
        }
        if (raw != null) {
            try {
                return Double.parseDouble(String.valueOf(raw));
            } catch (NumberFormatException ignored) {
                // 使用默认温度
            }
        }
        return 0.3;
    }

    private List<Long> parseMcpIds(Object raw) {
        if (!(raw instanceof List<?> list) || list.isEmpty()) {
            return List.of();
        }
        List<Long> ids = new ArrayList<>(list.size());
        for (Object item : list) {
            Long id = parseLong(item);
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    private Long parseLong(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(raw));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Long workflowId(WorkflowContext context) {
        Object id = context.getSysVariables().get("workflowId");
        if (id instanceof Number number) {
            return number.longValue();
        }
        return null;
    }
}
