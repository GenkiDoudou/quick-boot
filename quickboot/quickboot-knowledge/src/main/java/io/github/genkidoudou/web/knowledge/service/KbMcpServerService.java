package io.github.genkidoudou.web.knowledge.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.web.knowledge.dto.KbMcpOptionVo;
import io.github.genkidoudou.web.knowledge.dto.KbMcpServerBo;
import io.github.genkidoudou.web.knowledge.dto.KbMcpServerQueryBo;
import io.github.genkidoudou.web.knowledge.dto.KbMcpServerVo;
import io.github.genkidoudou.web.knowledge.dto.McpTestResultVo;

import java.util.List;
import java.util.Map;

/**
 * MCP 服务配置管理服务。
 */
public interface KbMcpServerService {

    /**
     * 分页查询 MCP 配置（密钥脱敏）。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageInfo<KbMcpServerVo> page(KbMcpServerQueryBo query);

    /**
     * 查询 MCP 详情。
     *
     * @param mcpId         MCP 主键
     * @param revealSecrets 是否展示密钥明文
     * @return 详情；不存在时返回 null
     */
    KbMcpServerVo getInfo(Long mcpId, boolean revealSecrets);

    /**
     * 新增 MCP 配置（含环境变量）。
     *
     * @param req 入参
     */
    void add(KbMcpServerBo req);

    /**
     * 修改 MCP 配置；SECRET 字段空串表示不修改原值。
     *
     * @param req 入参
     */
    void update(KbMcpServerBo req);

    /**
     * 逻辑删除 MCP 配置并驱逐客户端缓存。
     *
     * @param mcpIds MCP 主键列表
     */
    void removeBatch(List<Long> mcpIds);

    /**
     * 连接测试。
     *
     * @param mcpId MCP 主键
     * @return 测试结果
     */
    McpTestResultVo test(Long mcpId);

    /**
     * 导出 Cursor 兼容 mcp.json 片段。
     *
     * @param mcpIds         可选 ID 列表；为空时导出全部启用项
     * @param includeSecrets 是否包含明文密钥
     * @return {@code {mcpServers: {...}}}
     */
    Map<String, Object> export(List<Long> mcpIds, boolean includeSecrets);

    /**
     * 下拉选项（仅启用项，供知识库绑定）。
     *
     * @return 选项列表
     */
    List<KbMcpOptionVo> options();
}
