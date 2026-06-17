package io.github.genkidoudou.web.aiapp.service;

import io.github.genkidoudou.web.aiapp.domain.AiAppMessage;
import io.github.genkidoudou.web.aiapp.dto.AiAppMessageVo;

import java.util.List;
import java.util.Map;

/**
 * AI 应用消息持久化服务。
 */
public interface AiAppMessageService {

    /**
     * 列出会话消息（按时间升序）。
     *
     * @param sessionId 会话 ID
     * @return 消息列表
     */
    List<AiAppMessageVo> listBySession(Long sessionId);

    /**
     * 加载最近 N 轮对话（user+assistant 计为一轮的一部分）。
     *
     * @param sessionId    会话 ID
     * @param historyTurns 历史轮数上限
     * @return 消息实体列表
     */
    List<AiAppMessage> loadRecentHistory(Long sessionId, int historyTurns);

    /**
     * 保存用户消息。
     *
     * @param sessionId 会话 ID
     * @param content   内容
     * @return 消息 ID
     */
    Long saveUserMessage(Long sessionId, String content);

    /**
     * 保存助手消息。
     *
     * @param sessionId    会话 ID
     * @param content      内容
     * @param metadataJson 元数据 JSON
     * @return 消息 ID
     */
    Long saveAssistantMessage(Long sessionId, String content, String metadataJson);

    /**
     * 保存工具消息。
     *
     * @param sessionId    会话 ID
     * @param content      内容
     * @param metadataJson 元数据 JSON
     */
    void saveToolMessage(Long sessionId, String content, Map<String, Object> metadataJson);
}
