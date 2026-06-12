package io.github.genkidoudou.web.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.ai.domain.AiModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * AI 大模型配置 Mapper。
 */
@Mapper
public interface AiModelMapper extends BaseMapper<AiModel> {

    /**
     * 按知识库 ID 查询绑定的 Chat 模型 ID。
     *
     * @param kbId 知识库主键
     * @return 模型 ID；未绑定时返回 null
     */
    @Select("SELECT chat_model_id FROM kb_knowledge_base WHERE kb_id = #{kbId} AND deleted = 0 LIMIT 1")
    Long selectChatModelIdByKbId(@Param("kbId") Long kbId);

    /**
     * 按知识库 ID 查询绑定的 Embedding 模型 ID。
     *
     * @param kbId 知识库主键
     * @return 模型 ID；未绑定时返回 null
     */
    @Select("SELECT embedding_model_id FROM kb_knowledge_base WHERE kb_id = #{kbId} AND deleted = 0 LIMIT 1")
    Long selectEmbeddingModelIdByKbId(@Param("kbId") Long kbId);

    /**
     * 按工作流 ID 查询绑定的 Chat 模型 ID。
     *
     * @param workflowId 工作流主键
     * @return 模型 ID；未绑定时返回 null
     */
    @Select("SELECT chat_model_id FROM wf_workflow WHERE workflow_id = #{workflowId} AND deleted = 0 LIMIT 1")
    Long selectChatModelIdByWorkflowId(@Param("workflowId") Long workflowId);

    /**
     * 统计引用指定模型的知识库数量。
     *
     * @param modelId 模型主键
     * @return 引用数
     */
    @Select("""
        SELECT COUNT(1) FROM kb_knowledge_base
        WHERE deleted = 0
          AND (chat_model_id = #{modelId} OR embedding_model_id = #{modelId})
        """)
    int countKbRefByModelId(@Param("modelId") Long modelId);

    /**
     * 统计引用指定模型的工作流数量。
     *
     * @param modelId 模型主键
     * @return 引用数
     */
    @Select("SELECT COUNT(1) FROM wf_workflow WHERE deleted = 0 AND chat_model_id = #{modelId}")
    int countWfRefByModelId(@Param("modelId") Long modelId);

    /**
     * 按默认槽位查询启用中的模型 ID。
     *
     * @param defaultSlot 槽位：CHAT / EMBEDDING / WORKFLOW_CHAT
     * @return 模型 ID；无默认时返回 null
     */
    @Select("""
        SELECT model_id FROM ai_model
        WHERE default_slot = #{defaultSlot}
          AND status = 0
          AND deleted = 0
        LIMIT 1
        """)
    Long selectDefaultModelIdBySlot(@Param("defaultSlot") String defaultSlot);
}
