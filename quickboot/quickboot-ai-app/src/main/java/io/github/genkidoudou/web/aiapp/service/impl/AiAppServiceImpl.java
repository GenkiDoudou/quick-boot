package io.github.genkidoudou.web.aiapp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.github.genkidoudou.web.aiapp.constants.AiAppType;
import io.github.genkidoudou.web.aiapp.dto.AgentAppConfigDto;
import io.github.genkidoudou.web.aiapp.dto.WorkflowAppConfigDto;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.aiapp.constants.AiAppConstants;
import io.github.genkidoudou.web.aiapp.constants.AiAppStatus;
import io.github.genkidoudou.web.aiapp.domain.AiApp;
import io.github.genkidoudou.web.aiapp.dto.AiAppBo;
import io.github.genkidoudou.web.aiapp.dto.AiAppDetailVo;
import io.github.genkidoudou.web.aiapp.dto.AiAppPublishBo;
import io.github.genkidoudou.web.aiapp.dto.AiAppQueryBo;
import io.github.genkidoudou.web.aiapp.dto.AiAppVo;
import io.github.genkidoudou.web.aiapp.mapper.AiAppMapper;
import io.github.genkidoudou.web.aiapp.service.AiAppService;
import io.github.genkidoudou.web.aiapp.support.AiAppConfigValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 应用定义管理服务实现。
 */
@Service
public class AiAppServiceImpl implements AiAppService {

    private final AiAppMapper appMapper;
    private final AiAppConfigValidator configValidator;

    public AiAppServiceImpl(AiAppMapper appMapper, AiAppConfigValidator configValidator) {
        this.appMapper = appMapper;
        this.configValidator = configValidator;
    }

    @Override
    public PageInfo<AiAppVo> page(AiAppQueryBo query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();
        LambdaQueryWrapper<AiApp> wrapper = Wrappers.<AiApp>lambdaQuery()
            .eq(AiApp::getDelFlag, AiAppConstants.NOT_DELETED)
            .like(StrUtil.isNotBlank(query.getName()), AiApp::getName, query.getName())
            .eq(StrUtil.isNotBlank(query.getAppType()), AiApp::getAppType, query.getAppType())
            .eq(StrUtil.isNotBlank(query.getStatus()), AiApp::getStatus, query.getStatus())
            .orderByDesc(AiApp::getCreateTime);
        Page<AiApp> mp = appMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<AiAppVo> rows = new ArrayList<>();
        for (AiApp row : mp.getRecords()) {
            rows.add(BeanUtil.copyProperties(row, AiAppVo.class));
        }
        Page<AiAppVo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    @Override
    public AiAppDetailVo getDetail(Long appId) {
        AiApp app = requireApp(appId);
        return BeanUtil.copyProperties(app, AiAppDetailVo.class);
    }

    @Override
    public AiApp requireApp(Long appId) {
        AiApp app = appMapper.selectOne(Wrappers.<AiApp>lambdaQuery()
            .eq(AiApp::getId, appId)
            .eq(AiApp::getDelFlag, AiAppConstants.NOT_DELETED));
        if (app == null) {
            throw new WarningException(ErrorCodes.Biz.AI_APP_NOT_FOUND, "AI 应用不存在");
        }
        return app;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(AiAppBo req) {
        AiApp row = BeanUtil.copyProperties(req, AiApp.class);
        row.setStatus(AiAppStatus.DRAFT);
        row.setDelFlag(AiAppConstants.NOT_DELETED);
        if (StrUtil.isBlank(row.getConfigJson())) {
            row.setConfigJson(defaultConfigJson(row.getAppType()));
        }
        // 草稿创建不校验 chatModelId/workflowId，发布时由 publish() 严格校验
        appMapper.insert(row);
        return row.getId();
    }

    private String defaultConfigJson(String appType) {
        if (AiAppType.WORKFLOW.equals(appType)) {
            WorkflowAppConfigDto dto = new WorkflowAppConfigDto();
            dto.setMultiSession(true);
            return JSONUtil.toJsonStr(dto);
        }
        AgentAppConfigDto dto = new AgentAppConfigDto();
        dto.setHistoryTurns(10);
        dto.setMultiSession(true);
        return JSONUtil.toJsonStr(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AiAppBo req) {
        requireApp(req.getId());
        AiApp row = BeanUtil.copyProperties(req, AiApp.class);
        appMapper.updateById(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatch(List<Long> appIds) {
        if (appIds == null || appIds.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "删除应用ID不能为空");
        }
        for (Long appId : appIds) {
            AiApp row = new AiApp();
            row.setId(appId);
            row.setDelFlag(AiAppConstants.DELETED);
            appMapper.updateById(row);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(AiAppPublishBo req) {
        AiApp app = requireApp(req.getAppId());
        if (StrUtil.isBlank(app.getConfigJson())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "草稿配置为空，无法发布");
        }
        configValidator.validate(app);
        AiApp update = new AiApp();
        update.setId(app.getId());
        update.setPublishedConfigJson(app.getConfigJson());
        update.setStatus(AiAppStatus.PUBLISHED);
        appMapper.updateById(update);
    }
}
