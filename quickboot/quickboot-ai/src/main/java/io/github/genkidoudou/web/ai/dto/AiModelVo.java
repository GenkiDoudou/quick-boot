package io.github.genkidoudou.web.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 大模型详情/列表 VO。
 */
@Data
@Schema(description = "AI 大模型视图对象")
public class AiModelVo {

    @Schema(description = "模型 ID")
    private Long modelId;

    @Schema(description = "展示名称")
    private String name;

    @Schema(description = "唯一编码")
    private String code;

    @Schema(description = "备注说明")
    private String description;

    @Schema(description = "模型类型：CHAT / EMBEDDING")
    private String modelType;

    @Schema(description = "厂商：OPENAI_COMPAT / OLLAMA")
    private String provider;

    @Schema(description = "API 根地址")
    private String baseUrl;

    @Schema(description = "API Key 类型")
    private String apiKeyType;

    @Schema(description = "API Key（列表/详情默认脱敏）")
    private String apiKey;

    @Schema(description = "厂商模型名")
    private String modelName;

    @Schema(description = "Chat completions 路径")
    private String completionsPath;

    @Schema(description = "Embedding 路径")
    private String embeddingsPath;

    @Schema(description = "Embedding 向量维度")
    private Integer dimensions;

    @Schema(description = "Chat 默认温度")
    private BigDecimal temperature;

    @Schema(description = "Chat 最大 token")
    private Integer maxTokens;

    @Schema(description = "请求超时毫秒")
    private Integer requestTimeoutMs;

    @Schema(description = "全局默认槽位")
    private String defaultSlot;

    @Schema(description = "状态：0 正常 / 1 停用")
    private Integer status;

    @Schema(description = "最近探测状态")
    private String lastTestStatus;

    @Schema(description = "最近探测摘要")
    private String lastTestMsg;

    @Schema(description = "最近探测时间")
    private LocalDateTime lastTestTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
