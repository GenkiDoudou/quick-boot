package io.github.genkidoudou.web.ai.prompt.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 提示词优化会话实体，对应 {@code ai_prompt_optimize_session}。
 */
@Data
@TableName("ai_prompt_optimize_session")
public class AiPromptOptimizeSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "session_id", type = IdType.ASSIGN_ID)
    private Long sessionId;

    /** 所属提示词 ID。 */
    private Long promptId;

    /** 使用的 Chat 模型 ID。 */
    private Long modelId;

    /** 用户输入的优化目标。 */
    private String optimizeGoal;

    /** 优化前快照 JSON。 */
    private String originalSnapshot;

    /** 模型输出解析结果或原始文本 JSON。 */
    private String resultSnapshot;

    /**
     * 会话状态：SUCCESS / FAILED。
     *
     * @see io.github.genkidoudou.web.ai.prompt.constants.AiPromptOptimizeStatus
     */
    private String status;

    /** 失败原因摘要。 */
    private String errorMsg;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
