package io.github.genkidoudou.web.ai.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.web.ai.dto.AiModelBo;
import io.github.genkidoudou.web.ai.dto.AiModelOptionVo;
import io.github.genkidoudou.web.ai.dto.AiModelQueryBo;
import io.github.genkidoudou.web.ai.dto.AiModelVo;
import io.github.genkidoudou.web.ai.dto.AiSetDefaultBo;
import io.github.genkidoudou.web.ai.dto.AiTestResultVo;

import java.util.List;
import java.util.Map;

/**
 * AI 大模型配置管理服务。
 */
public interface AiModelService {

    /**
     * 分页查询模型配置（密钥脱敏）。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageInfo<AiModelVo> page(AiModelQueryBo query);

    /**
     * 查询模型详情。
     *
     * @param modelId       模型主键
     * @param revealSecrets 是否展示密钥明文
     * @return 详情；不存在时返回 null
     */
    AiModelVo getInfo(Long modelId, boolean revealSecrets);

    /**
     * 新增模型配置。
     *
     * @param req 入参
     */
    void add(AiModelBo req);

    /**
     * 修改模型配置；SECRET 字段空串表示不修改原值。
     *
     * @param req 入参
     */
    void update(AiModelBo req);

    /**
     * 逻辑删除模型并驱逐 Registry 缓存。
     *
     * @param modelIds 模型主键列表
     */
    void removeBatch(List<Long> modelIds);

    /**
     * 连接测试。
     *
     * @param modelId 模型主键
     * @return 测试结果
     */
    AiTestResultVo test(Long modelId);

    /**
     * 设为全局默认模型（同槽位旧默认会被清除）。
     *
     * @param req 入参
     */
    void setDefault(AiSetDefaultBo req);

    /**
     * 清除指定槽位的全局默认。
     *
     * @param defaultSlot 槽位
     */
    void clearDefault(String defaultSlot);

    /**
     * 导出 Spring AI YAML 片段或 ENV 变量清单。
     *
     * @param modelIds         可选 ID 列表；为空时导出全部启用项
     * @param format           yaml 或 env
     * @param includeSecrets   是否包含明文密钥
     * @return 导出内容
     */
    String export(List<Long> modelIds, String format, boolean includeSecrets);

    /**
     * 下拉选项（仅启用项）。
     *
     * @param modelType 可选类型过滤：CHAT / EMBEDDING
     * @return 选项列表
     */
    List<AiModelOptionVo> options(String modelType);

    /**
     * 从当前 spring.ai YAML 配置生成预置条目草稿（不落库）。
     *
     * @return 草稿列表
     */
    List<AiModelVo> importFromYaml();
}
