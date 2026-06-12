package io.github.genkidoudou.web.knowledge.domain;

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
 * 知识库实体，对应表 {@code kb_knowledge_base}。
 */
@Data
@TableName("kb_knowledge_base")
public class KbKnowledgeBase implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "kb_id", type = IdType.ASSIGN_ID)
    private Long kbId;

    /** 知识库名称。 */
    private String name;

    /** 描述说明。 */
    private String description;

    /** 分块 token 上限。 */
    private Integer chunkSize;

    /** 分块重叠 token 数。 */
    private Integer chunkOverlap;

    /**
     * 默认分段模式：AUTO / CUSTOM。
     *
     * @see io.github.genkidoudou.web.knowledge.constants.KbSegmentMode
     */
    private String segmentMode;

    /**
     * 默认自定义分隔符：SINGLE_NEWLINE / DOUBLE_NEWLINE（仅 CUSTOM 生效）。
     *
     * @see io.github.genkidoudou.web.knowledge.constants.KbChunkDelimiter
     */
    private String chunkDelimiter;

    /** 默认预处理：归一化连续空白，0 否 / 1 是。 */
    private Integer preprocessNormalizeWs;

    /** 默认预处理：删除 URL，0 否 / 1 是。 */
    private Integer preprocessRemoveUrl;

    /** 默认预处理：删除电子邮箱，0 否 / 1 是。 */
    private Integer preprocessRemoveEmail;

    /** 状态：0 正常 / 1 停用。 */
    private Integer status;

    /** 可选 Chat 模型 ai_model.model_id。 */
    private Long chatModelId;

    /** 可选 Embedding 模型 ai_model.model_id。 */
    private Long embeddingModelId;

    /** 逻辑删除：0 否 / 1 是（手动维护，非 TableLogic）。 */
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
