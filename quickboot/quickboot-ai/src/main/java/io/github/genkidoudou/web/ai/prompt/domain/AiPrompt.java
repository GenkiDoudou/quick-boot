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
 * 提示词模板主表实体，对应 {@code ai_prompt}。
 */
@Data
@TableName("ai_prompt")
public class AiPrompt implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "prompt_id", type = IdType.ASSIGN_ID)
    private Long promptId;

    /** 唯一编码。 */
    private String code;

    /** 展示名称。 */
    private String name;

    /** 备注说明。 */
    private String description;

    /**
     * 场景类型：LLM / RAG / CLASSIFIER / EXTRACTOR / CUSTOM。
     *
     * @see io.github.genkidoudou.web.ai.prompt.constants.AiPromptType
     */
    private String promptType;

    /** 业务域。 */
    private String domain;

    /** 分类。 */
    private String category;

    /** 标签 JSON 数组字符串。 */
    private String tags;

    /**
     * 状态：DRAFT / PUBLISHED / ARCHIVED。
     *
     * @see io.github.genkidoudou.web.ai.prompt.constants.AiPromptStatus
     */
    private String status;

    /** 当前已发布版本 ID。 */
    private Long currentVersionId;

    /** 当前已发布版本号（展示用，从 1 递增）。 */
    private Integer currentVersionNo;

    /** 优化/A/B 默认 Chat 模型 ID。 */
    private Long optimizeModelId;

    /** 逻辑删除：0 否 / 1 是。 */
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
