package io.github.genkidoudou.web.aiapp.service;

import io.github.genkidoudou.web.aiapp.dto.AiAppChatBo;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 智能体应用聊天服务（SSE + Tool Calling）。
 */
public interface AiAppChatService {

    /**
     * 智能体模式 SSE 对话。
     *
     * @param req     聊天入参
     * @param userKey 用户标识
     * @return SSE 发射器
     */
    SseEmitter streamChat(AiAppChatBo req, String userKey);
}
