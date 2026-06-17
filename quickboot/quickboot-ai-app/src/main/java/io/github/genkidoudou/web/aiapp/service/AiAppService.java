package io.github.genkidoudou.web.aiapp.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.web.aiapp.domain.AiApp;
import io.github.genkidoudou.web.aiapp.dto.AiAppBo;
import io.github.genkidoudou.web.aiapp.dto.AiAppDetailVo;
import io.github.genkidoudou.web.aiapp.dto.AiAppPublishBo;
import io.github.genkidoudou.web.aiapp.dto.AiAppQueryBo;
import io.github.genkidoudou.web.aiapp.dto.AiAppVo;

import java.util.List;

/**
 * AI 应用定义管理服务。
 */
public interface AiAppService {

    /**
     * 分页查询应用列表。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageInfo<AiAppVo> page(AiAppQueryBo query);

    /**
     * 获取应用详情（含 config）。
     *
     * @param appId 应用 ID
     * @return 详情
     */
    AiAppDetailVo getDetail(Long appId);

    /**
     * 加载应用实体，不存在时抛业务异常。
     *
     * @param appId 应用 ID
     * @return 实体
     */
    AiApp requireApp(Long appId);

    /**
     * 新增应用草稿。
     *
     * @param req 入参
     * @return 新应用 ID
     */
    Long add(AiAppBo req);

    /**
     * 更新应用草稿元数据与 config。
     *
     * @param req 入参
     */
    void update(AiAppBo req);

    /**
     * 批量逻辑删除应用。
     *
     * @param appIds 应用 ID 列表
     */
    void removeBatch(List<Long> appIds);

    /**
     * 发布应用：快照 config_json 到 published_config_json。
     *
     * @param req 发布入参
     */
    void publish(AiAppPublishBo req);
}
