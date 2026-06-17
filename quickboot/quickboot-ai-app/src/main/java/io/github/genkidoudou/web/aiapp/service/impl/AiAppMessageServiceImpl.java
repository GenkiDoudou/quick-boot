package io.github.genkidoudou.web.aiapp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.web.aiapp.constants.AiAppMessageRole;
import io.github.genkidoudou.web.aiapp.domain.AiAppMessage;
import io.github.genkidoudou.web.aiapp.dto.AiAppMessageVo;
import io.github.genkidoudou.web.aiapp.mapper.AiAppMessageMapper;
import io.github.genkidoudou.web.aiapp.service.AiAppMessageService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * AI 应用消息持久化服务实现。
 */
@Service
public class AiAppMessageServiceImpl implements AiAppMessageService {

    private final AiAppMessageMapper messageMapper;

    public AiAppMessageServiceImpl(AiAppMessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    @Override
    public List<AiAppMessageVo> listBySession(Long sessionId) {
        List<AiAppMessage> rows = messageMapper.selectList(Wrappers.<AiAppMessage>lambdaQuery()
            .eq(AiAppMessage::getSessionId, sessionId)
            .orderByAsc(AiAppMessage::getCreateTime));
        List<AiAppMessageVo> result = new ArrayList<>();
        for (AiAppMessage row : rows) {
            result.add(BeanUtil.copyProperties(row, AiAppMessageVo.class));
        }
        return result;
    }

    @Override
    public List<AiAppMessage> loadRecentHistory(Long sessionId, int historyTurns) {
        if (historyTurns <= 0) {
            return Collections.emptyList();
        }
        int limit = historyTurns * 2;
        List<AiAppMessage> rows = messageMapper.selectList(Wrappers.<AiAppMessage>lambdaQuery()
            .eq(AiAppMessage::getSessionId, sessionId)
            .in(AiAppMessage::getRole, AiAppMessageRole.USER, AiAppMessageRole.ASSISTANT)
            .orderByDesc(AiAppMessage::getCreateTime)
            .last("LIMIT " + limit));
        Collections.reverse(rows);
        return rows;
    }

    @Override
    public Long saveUserMessage(Long sessionId, String content) {
        return saveMessage(sessionId, AiAppMessageRole.USER, content, null);
    }

    @Override
    public Long saveAssistantMessage(Long sessionId, String content, String metadataJson) {
        return saveMessage(sessionId, AiAppMessageRole.ASSISTANT, content, metadataJson);
    }

    @Override
    public void saveToolMessage(Long sessionId, String content, Map<String, Object> metadataJson) {
        saveMessage(sessionId, AiAppMessageRole.TOOL, content,
            metadataJson == null ? null : JSONUtil.toJsonStr(metadataJson));
    }

    private Long saveMessage(Long sessionId, String role, String content, String metadataJson) {
        AiAppMessage msg = new AiAppMessage();
        msg.setSessionId(sessionId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setMetadataJson(metadataJson);
        msg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(msg);
        return msg.getId();
    }
}
