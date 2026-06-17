package io.github.genkidoudou.web.aiapp.service;

import io.github.genkidoudou.web.aiapp.domain.AiAppPublish;
import io.github.genkidoudou.web.aiapp.dto.AiAppPublishVo;

/**
 * AI 应用发布与嵌入配置服务。
 */
public interface AiAppPublishService {

    /**
     * 获取应用嵌入/菜单配置（管理端）。
     *
     * @param appId 应用 ID
     * @return 配置 VO；无记录时返回空壳
     */
    AiAppPublishVo getEmbedInfo(Long appId);

    /**
     * 保存嵌入/菜单配置；首次保存时生成 embed_token。
     *
     * @param req 配置入参
     */
    void saveEmbed(AiAppPublishVo req);

    /**
     * 按 embed_token 加载发布配置。
     *
     * @param token 嵌入令牌
     * @return 发布实体
     */
    AiAppPublish requireByToken(String token);

    /**
     * 校验 Origin 是否在白名单内。
     *
     * @param publish 发布配置
     * @param origin  请求 Origin
     */
    void validateOrigin(AiAppPublish publish, String origin);
}
