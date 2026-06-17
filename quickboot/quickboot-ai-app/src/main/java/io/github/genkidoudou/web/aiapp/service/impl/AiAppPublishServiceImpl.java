package io.github.genkidoudou.web.aiapp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.aiapp.constants.AiAppConstants;
import io.github.genkidoudou.web.aiapp.constants.AiAppStatus;
import io.github.genkidoudou.web.aiapp.domain.AiApp;
import io.github.genkidoudou.web.aiapp.domain.AiAppPublish;
import io.github.genkidoudou.web.aiapp.dto.AiAppPublishVo;
import io.github.genkidoudou.web.aiapp.mapper.AiAppPublishMapper;
import io.github.genkidoudou.web.aiapp.service.AiAppPublishService;
import io.github.genkidoudou.web.aiapp.service.AiAppService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Locale;

/**
 * AI 应用发布与嵌入配置服务实现。
 */
@Service
public class AiAppPublishServiceImpl implements AiAppPublishService {

    private final AiAppPublishMapper publishMapper;
    private final AiAppService appService;

    public AiAppPublishServiceImpl(AiAppPublishMapper publishMapper, AiAppService appService) {
        this.publishMapper = publishMapper;
        this.appService = appService;
    }

    @Override
    public AiAppPublishVo getEmbedInfo(Long appId) {
        appService.requireApp(appId);
        AiAppPublish row = findByAppId(appId);
        if (row == null) {
            AiAppPublishVo empty = new AiAppPublishVo();
            empty.setAppId(appId);
            empty.setEnabled(false);
            return empty;
        }
        AiAppPublishVo vo = BeanUtil.copyProperties(row, AiAppPublishVo.class);
        vo.setEnabled(row.getEnabled() != null && row.getEnabled() == AiAppConstants.EMBED_ENABLED);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveEmbed(AiAppPublishVo req) {
        AiApp app = appService.requireApp(req.getAppId());
        if (!AiAppStatus.PUBLISHED.equals(app.getStatus())) {
            throw new WarningException(ErrorCodes.Biz.STATE_NOT_ALLOWED, "仅已发布应用可配置嵌入");
        }
        AiAppPublish existing = findByAppId(req.getAppId());
        if (existing == null) {
            AiAppPublish row = new AiAppPublish();
            row.setAppId(req.getAppId());
            row.setEmbedToken(generateToken());
            row.setAllowedOrigins(StrUtil.blankToDefault(req.getAllowedOrigins(), ""));
            row.setMenuPath(StrUtil.blankToDefault(req.getMenuPath(), ""));
            row.setMenuComponent(StrUtil.blankToDefault(req.getMenuComponent(), ""));
            row.setEnabled(Boolean.TRUE.equals(req.getEnabled()) ? AiAppConstants.EMBED_ENABLED : AiAppConstants.EMBED_DISABLED);
            publishMapper.insert(row);
        } else {
            AiAppPublish update = new AiAppPublish();
            update.setId(existing.getId());
            if (StrUtil.isNotBlank(req.getAllowedOrigins())) {
                update.setAllowedOrigins(req.getAllowedOrigins());
            }
            if (req.getMenuPath() != null) {
                update.setMenuPath(req.getMenuPath());
            }
            if (req.getMenuComponent() != null) {
                update.setMenuComponent(req.getMenuComponent());
            }
            if (req.getEnabled() != null) {
                update.setEnabled(Boolean.TRUE.equals(req.getEnabled())
                    ? AiAppConstants.EMBED_ENABLED : AiAppConstants.EMBED_DISABLED);
            }
            publishMapper.updateById(update);
        }
    }

    @Override
    public AiAppPublish requireByToken(String token) {
        if (StrUtil.isBlank(token)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "嵌入令牌无效");
        }
        AiAppPublish row = publishMapper.selectOne(Wrappers.<AiAppPublish>lambdaQuery()
            .eq(AiAppPublish::getEmbedToken, token)
            .last("LIMIT 1"));
        if (row == null || row.getEnabled() == null || row.getEnabled() != AiAppConstants.EMBED_ENABLED) {
            throw new WarningException(ErrorCodes.Biz.AI_APP_NOT_FOUND, "嵌入配置不存在或未启用");
        }
        AiApp app = appService.requireApp(row.getAppId());
        if (!AiAppStatus.PUBLISHED.equals(app.getStatus())) {
            throw new WarningException(ErrorCodes.Biz.STATE_NOT_ALLOWED, "应用未发布，无法嵌入访问");
        }
        return row;
    }

    @Override
    public void validateOrigin(AiAppPublish publish, String origin) {
        if (StrUtil.isBlank(publish.getAllowedOrigins())) {
            return;
        }
        if (StrUtil.isBlank(origin)) {
            throw new WarningException(ErrorCodes.Security.HOST_NOT_ALLOWED, "来源域名不在白名单内");
        }
        String normalizedOrigin = origin.trim().toLowerCase(Locale.ROOT);
        boolean allowed = Arrays.stream(publish.getAllowedOrigins().split(","))
            .map(String::trim)
            .filter(StrUtil::isNotBlank)
            .anyMatch(item -> normalizedOrigin.equals(item.toLowerCase(Locale.ROOT))
                || normalizedOrigin.endsWith(item.toLowerCase(Locale.ROOT).replace("*", "")));
        if (!allowed) {
            throw new WarningException(ErrorCodes.Security.HOST_NOT_ALLOWED, "来源域名不在白名单内");
        }
    }

    private AiAppPublish findByAppId(Long appId) {
        return publishMapper.selectOne(Wrappers.<AiAppPublish>lambdaQuery()
            .eq(AiAppPublish::getAppId, appId)
            .last("LIMIT 1"));
    }

    /**
     * 生成 48 位随机嵌入令牌。
     *
     * @return token 字符串
     */
    private String generateToken() {
        return RandomUtil.randomString(48);
    }
}
