package io.github.genkidoudou.web.aiapp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.aiapp.dto.AgentAppConfigDto;
import io.github.genkidoudou.web.aiapp.service.AiAppVariableService;
import io.github.genkidoudou.web.aiapp.support.AiAppAiGuard;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 应用变量记忆服务实现。
 */
@Service
public class AiAppVariableServiceImpl implements AiAppVariableService {

    private static final String EXTRACT_PROMPT = """
        你是变量抽取助手。根据对话内容，仅更新下列已声明变量的值。
        只输出 JSON 对象，键为变量名，值为字符串；未提及的变量保持原值或省略。
        不要输出 markdown 或解释文字。
        """;

    private final AiAppAiGuard aiGuard;

    public AiAppVariableServiceImpl(AiAppAiGuard aiGuard) {
        this.aiGuard = aiGuard;
    }

    @Override
    public String injectVariables(String basePrompt,
                                  List<AgentAppConfigDto.MemoryVariableDto> memoryVariables,
                                  Map<String, String> currentVariables) {
        if (memoryVariables == null || memoryVariables.isEmpty()) {
            return StrUtil.blankToDefault(basePrompt, "");
        }
        StringBuilder sb = new StringBuilder(StrUtil.blankToDefault(basePrompt, ""));
        sb.append("\n\n【会话变量】\n");
        for (AgentAppConfigDto.MemoryVariableDto var : memoryVariables) {
            if (var == null || StrUtil.isBlank(var.getKey())) {
                continue;
            }
            String value = currentVariables.getOrDefault(var.getKey(),
                StrUtil.blankToDefault(var.getDefaultValue(), ""));
            sb.append("- {{").append(var.getKey()).append("}}: ").append(value);
            if (StrUtil.isNotBlank(var.getDescription())) {
                sb.append(" (").append(var.getDescription()).append(")");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public Map<String, String> parseVariables(String variablesJson) {
        Map<String, String> result = new HashMap<>();
        if (StrUtil.isBlank(variablesJson)) {
            return result;
        }
        JSONObject obj = JSONUtil.parseObj(variablesJson);
        for (String key : obj.keySet()) {
            Object val = obj.get(key);
            result.put(key, val == null ? "" : String.valueOf(val));
        }
        return result;
    }

    @Override
    public String extractAfterTurn(List<AgentAppConfigDto.MemoryVariableDto> memoryVariables,
                                   Map<String, String> currentVariables,
                                   String userMessage,
                                   String assistantReply,
                                   Long chatModelId) {
        if (memoryVariables == null || memoryVariables.isEmpty()) {
            return JSONUtil.toJsonStr(currentVariables);
        }
        Map<String, String> merged = new HashMap<>(currentVariables);
        try {
            StringBuilder varDesc = new StringBuilder();
            for (AgentAppConfigDto.MemoryVariableDto var : memoryVariables) {
                if (var != null && StrUtil.isNotBlank(var.getKey())) {
                    varDesc.append(var.getKey()).append(": ").append(var.getDescription()).append("\n");
                }
            }
            ChatClient client = ChatClient.builder(aiGuard.requireChatModel(chatModelId))
                .defaultSystem(EXTRACT_PROMPT)
                .build();
            String prompt = "变量声明：\n" + varDesc
                + "\n当前值：\n" + JSONUtil.toJsonStr(merged)
                + "\n\n用户：\n" + userMessage
                + "\n\n助手：\n" + assistantReply;
            String json = client.prompt().user(prompt).call().content();
            if (StrUtil.isNotBlank(json)) {
                String trimmed = json.trim();
                if (trimmed.startsWith("```")) {
                    trimmed = trimmed.replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "");
                }
                JSONObject extracted = JSONUtil.parseObj(trimmed);
                for (String key : extracted.keySet()) {
                    merged.put(key, String.valueOf(extracted.get(key)));
                }
            }
        } catch (Exception ignored) {
            // 抽取失败时保留原变量，不阻断主流程
        }
        return JSONUtil.toJsonStr(merged);
    }
}
