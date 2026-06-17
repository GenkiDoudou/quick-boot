package io.github.genkidoudou.web.aiapp.service;

import io.github.genkidoudou.web.aiapp.domain.AiAppSession;
import io.github.genkidoudou.web.aiapp.dto.AiAppSessionBo;
import io.github.genkidoudou.web.aiapp.dto.AiAppSessionVo;

import java.util.List;

/**
 * AI 应用会话管理服务。
 */
public interface AiAppSessionService {

    /**
     * 按应用与用户标识列出会话。
     *
     * @param appId   应用 ID
     * @param userKey 用户标识
     * @return 会话列表
     */
    List<AiAppSessionVo> listByAppAndUser(Long appId, String userKey);

    /**
     * 新建会话。
     *
     * @param req     入参
     * @param userKey 用户标识
     * @return 会话 ID
     */
    Long add(AiAppSessionBo req, String userKey);

    /**
     * 删除会话及其消息。
     *
     * @param sessionId 会话 ID
     * @param userKey   用户标识（隔离校验）
     */
    void remove(Long sessionId, String userKey);

    /**
     * 加载会话并校验归属。
     *
     * @param sessionId 会话 ID
     * @param userKey   用户标识
     * @return 会话实体
     */
    AiAppSession requireSession(Long sessionId, String userKey);

    /**
     * 更新会话变量 JSON。
     *
     * @param sessionId     会话 ID
     * @param variablesJson 变量 JSON
     */
    void updateVariables(Long sessionId, String variablesJson);

    /**
     * 更新会话标题（首条消息摘要）。
     *
     * @param sessionId 会话 ID
     * @param title     标题
     */
    void updateTitleIfBlank(Long sessionId, String title);
}
