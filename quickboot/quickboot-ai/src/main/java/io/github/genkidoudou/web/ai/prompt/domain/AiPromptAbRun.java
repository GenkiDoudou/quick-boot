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
 * 提示词 A/B 对比运行记录实体，对应 {@code ai_prompt_ab_run}。
 */
@Data
@TableName("ai_prompt_ab_run")
public class AiPromptAbRun implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "run_id", type = IdType.ASSIGN_ID)
    private Long runId;

    /** 所属提示词 ID。 */
    private Long promptId;

    /** 使用的 Chat 模型 ID。 */
    private Long modelId;

    /** 版本 A（0 表示当前草稿快照）。 */
    private Long variantAVersionId;

    /** 版本 B（0 表示当前草稿快照）。 */
    private Long variantBVersionId;

    /** 样例变量键值 JSON。 */
    private String sampleInputJson;

    /** 渲染后完整 prompt A（审计）。 */
    private String renderedPromptA;

    /** 渲染后完整 prompt B（审计）。 */
    private String renderedPromptB;

    /** 模型输出 A。 */
    private String outputA;

    /** 模型输出 B。 */
    private String outputB;

    /** 人工评分 A（1–5）。 */
    private Integer scoreA;

    /** 人工评分 B（1–5）。 */
    private Integer scoreB;

    /** 胜者：A / B / TIE。 */
    private String winner;

    /** 评分备注。 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
