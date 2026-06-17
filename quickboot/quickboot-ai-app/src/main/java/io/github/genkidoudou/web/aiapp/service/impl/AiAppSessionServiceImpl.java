package io.github.genkidoudou.web.aiapp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.aiapp.domain.AiAppSession;
import io.github.genkidoudou.web.aiapp.dto.AiAppSessionBo;
import io.github.genkidoudou.web.aiapp.dto.AiAppSessionVo;
import io.github.genkidoudou.web.aiapp.mapper.AiAppMessageMapper;
import io.github.genkidoudou.web.aiapp.mapper.AiAppSessionMapper;
import io.github.genkidoudou.web.aiapp.service.AiAppService;
import io.github.genkidoudou.web.aiapp.service.AiAppSessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 应用会话管理服务实现。
 */
@Service
public class AiAppSessionServiceImpl implements AiAppSessionService {

    private final AiAppSessionMapper sessionMapper;
    private final AiAppMessageMapper messageMapper;
    private final AiAppService appService;

    public AiAppSessionServiceImpl(AiAppSessionMapper sessionMapper,
                                   AiAppMessageMapper messageMapper,
                                   AiAppService appService) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.appService = appService;
    }

    @Override
    public List<AiAppSessionVo> listByAppAndUser(Long appId, String userKey) {
        appService.requireApp(appId);
        List<AiAppSession> rows = sessionMapper.selectList(Wrappers.<AiAppSession>lambdaQuery()
            .eq(AiAppSession::getAppId, appId)
            .eq(AiAppSession::getUserKey, userKey)
            .orderByDesc(AiAppSession::getUpdateTime));
        List<AiAppSessionVo> result = new ArrayList<>();
        for (AiAppSession row : rows) {
            result.add(BeanUtil.copyProperties(row, AiAppSessionVo.class));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(AiAppSessionBo req, String userKey) {
        appService.requireApp(req.getAppId());
        AiAppSession session = new AiAppSession();
        session.setAppId(req.getAppId());
        session.setUserKey(userKey);
        session.setTitle(StrUtil.blankToDefault(req.getTitle(), "新会话"));
        session.setVariablesJson("{}");
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        sessionMapper.insert(session);
        return session.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long sessionId, String userKey) {
        AiAppSession session = requireSession(sessionId, userKey);
        messageMapper.delete(Wrappers.lambdaQuery(io.github.genkidoudou.web.aiapp.domain.AiAppMessage.class)
            .eq(io.github.genkidoudou.web.aiapp.domain.AiAppMessage::getSessionId, session.getId()));
        sessionMapper.deleteById(sessionId);
    }

    @Override
    public AiAppSession requireSession(Long sessionId, String userKey) {
        AiAppSession session = sessionMapper.selectById(sessionId);
        if (session == null || !userKey.equals(session.getUserKey())) {
            throw new WarningException(ErrorCodes.Biz.AI_APP_NOT_FOUND, "会话不存在或无权访问");
        }
        return session;
    }

    @Override
    public void updateVariables(Long sessionId, String variablesJson) {
        AiAppSession update = new AiAppSession();
        update.setId(sessionId);
        update.setVariablesJson(variablesJson);
        update.setUpdateTime(LocalDateTime.now());
        sessionMapper.updateById(update);
    }

    @Override
    public void updateTitleIfBlank(Long sessionId, String title) {
        AiAppSession session = sessionMapper.selectById(sessionId);
        if (session == null || StrUtil.isNotBlank(session.getTitle()) && !"新会话".equals(session.getTitle())) {
            return;
        }
        String summary = StrUtil.sub(title, 0, 50);
        AiAppSession update = new AiAppSession();
        update.setId(sessionId);
        update.setTitle(summary);
        update.setUpdateTime(LocalDateTime.now());
        sessionMapper.updateById(update);
    }
}
