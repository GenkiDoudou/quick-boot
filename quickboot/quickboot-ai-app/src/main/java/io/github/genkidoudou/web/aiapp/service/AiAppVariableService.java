package io.github.genkidoudou.web.aiapp.service;

import io.github.genkidoudou.web.aiapp.dto.AgentAppConfigDto;

import java.util.Map;

/**
 * AI 应用变量记忆服务：注入 system prompt 与轮次后抽取更新。
 */
public interface AiAppVariableService {

    /**
     * 将当前变量值注入 system prompt。
     *
     * @param basePrompt       用户 systemPrompt
     * @param memoryVariables  变量声明
     * @param currentVariables 当前变量值
     * @return 增强后的 system prompt
     */
    String injectVariables(String basePrompt,
                           java.util.List<AgentAppConfigDto.MemoryVariableDto> memoryVariables,
                           Map<String, String> currentVariables);

    /**
     * 解析会话 variables_json 为 Map。
     *
     * @param variablesJson JSON 字符串
     * @return 变量 Map
     */
    Map<String, String> parseVariables(String variablesJson);

    /**
     * 轮次结束后从对话中抽取变量更新（轻量 LLM 抽取）。
     *
     * @param memoryVariables  变量声明
     * @param currentVariables 当前变量
     * @param userMessage      用户消息
     * @param assistantReply   助手回复
     * @param chatModelId      模型 ID
     * @return 更新后的 variables JSON
     */
    String extractAfterTurn(java.util.List<AgentAppConfigDto.MemoryVariableDto> memoryVariables,
                            Map<String, String> currentVariables,
                            String userMessage,
                            String assistantReply,
                            Long chatModelId);
}
