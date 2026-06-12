package io.github.genkidoudou.web.knowledge.service;

import java.util.List;

/**
 * 知识库与外部 MCP 绑定关系维护。
 */
public interface KbKnowledgeBaseMcpService {

    /**
     * 查询知识库绑定的启用 MCP ID 列表（按 order_num 排序）。
     *
     * @param kbId 知识库 ID
     * @return MCP 主键列表
     */
    List<Long> listEnabledMcpIdsByKbId(Long kbId);

    /**
     * 查询知识库绑定的全部 MCP ID（含停用 MCP，供表单回显）。
     *
     * @param kbId 知识库 ID
     * @return MCP 主键列表
     */
    List<Long> listMcpIdsByKbId(Long kbId);

    /**
     * 保存知识库 MCP 绑定（全量替换）。
     *
     * @param kbId   知识库 ID
     * @param mcpIds MCP 主键列表，可为空表示清空
     */
    void saveBindings(Long kbId, List<Long> mcpIds);

    /**
     * 删除知识库时级联清理绑定。
     *
     * @param kbId 知识库 ID
     */
    void removeByKbId(Long kbId);
}
