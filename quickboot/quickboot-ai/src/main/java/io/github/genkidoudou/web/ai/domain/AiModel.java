package io.github.genkidoudou.web.ai.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 大模型配置实体，对应表 {@code ai_model}。
 */
@Data
@TableName("ai_model")
public class AiModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "model_id", type = IdType.ASSIGN_ID)
    private Long modelId;

    /** 展示名称。 */
    private String name;

    /** 唯一编码，导出 YAML 时作为稳定标识。 */
    private String code;

    /** 备注说明。 */
    private String description;

    /**
     * 模型类型：CHAT / EMBEDDING。
     *
     * @see io.github.genkidoudou.web.ai.constants.AiModelType
     */
    private String modelType;

    /**
     * 厂商协议：OPENAI_COMPAT / OLLAMA。
     *
     * @see io.github.genkidoudou.web.ai.constants.AiProvider
     */
    private String provider;

    /** API 根地址。 */
    private String baseUrl;

    /**
     * API Key 存储类型：PLAIN / SECRET / ENV_REF。
     *
     * @see io.github.genkidoudou.web.ai.constants.AiApiKeyType
     */
    private String apiKeyType;

    /** 按 apiKeyType 解释的密钥值。 */
    private String apiKey;

    /** 厂商模型名。 */
    private String modelName;

    /** OpenAI 兼容 Chat completions 路径覆盖。 */
    private String completionsPath;

    /** OpenAI 兼容 Embedding 路径覆盖。 */
    private String embeddingsPath;

    /** Embedding 向量维度（EMBEDDING 必填）。 */
    private Integer dimensions;

    /** Chat 默认温度。 */
    private BigDecimal temperature;

    /** Chat 最大 token 上限。 */
    private Integer maxTokens;

    /** 请求超时（毫秒）。 */
    private Integer requestTimeoutMs;

    /**
     * 全局默认槽位：CHAT / EMBEDDING / WORKFLOW_CHAT；同槽位仅一条启用记录。
     *
     * @see io.github.genkidoudou.web.ai.constants.AiDefaultSlot
     */
    private String defaultSlot;

    /** 状态：0 正常 / 1 停用。 */
    private Integer status;

    /**
     * 最近探测状态：SUCCESS / FAILED / UNTESTED。
     *
     * @see io.github.genkidoudou.web.ai.constants.AiTestStatus
     */
    private String lastTestStatus;

    /** 最近探测摘要。 */
    private String lastTestMsg;

    /** 最近探测时间。 */
    private LocalDateTime lastTestTime;

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
