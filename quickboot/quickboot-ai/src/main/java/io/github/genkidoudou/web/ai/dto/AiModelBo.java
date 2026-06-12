package io.github.genkidoudou.web.ai.dto;

import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * AI 大模型新增/修改入参。
 */
@Data
@Schema(description = "AI 大模型业务入参")
public class AiModelBo {

    @NotNull(message = "模型 ID 不能为空", groups = UpdateGroup.class)
    @Schema(description = "模型 ID（修改必填）")
    private Long modelId;

    @NotBlank(message = "名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 100, message = "名称长度不能超过100", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "展示名称")
    private String name;

    @NotBlank(message = "编码不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 64, message = "编码长度不能超过64", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "唯一编码")
    private String code;

    @Size(max = 500, message = "描述长度不能超过500", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "备注说明")
    private String description;

    @NotBlank(message = "模型类型不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "模型类型：CHAT / EMBEDDING")
    private String modelType;

    @NotBlank(message = "Provider 不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "厂商：OPENAI_COMPAT / OLLAMA")
    private String provider;

    @NotBlank(message = "baseUrl 不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 2048, message = "baseUrl 长度不能超过2048", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "API 根地址")
    private String baseUrl;

    @Schema(description = "API Key 类型：PLAIN / SECRET / ENV_REF")
    private String apiKeyType;

    @Size(max = 2000, message = "apiKey 长度不能超过2000", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "API Key 值；SECRET 类型空串表示不修改")
    private String apiKey;

    @NotBlank(message = "模型名不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 128, message = "模型名长度不能超过128", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "厂商模型名")
    private String modelName;

    @Size(max = 128, message = "completionsPath 长度不能超过128", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "Chat completions 路径覆盖")
    private String completionsPath;

    @Size(max = 128, message = "embeddingsPath 长度不能超过128", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "Embedding 路径覆盖")
    private String embeddingsPath;

    @Min(value = 1, message = "dimensions 必须大于 0", groups = {AddGroup.class, UpdateGroup.class})
    @Max(value = 8192, message = "dimensions 不能超过 8192", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "Embedding 向量维度")
    private Integer dimensions;

    @DecimalMin(value = "0.0", message = "temperature 不能小于 0", groups = {AddGroup.class, UpdateGroup.class})
    @DecimalMax(value = "2.0", message = "temperature 不能大于 2", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "Chat 默认温度")
    private BigDecimal temperature;

    @Min(value = 1, message = "maxTokens 必须大于 0", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "Chat 最大 token")
    private Integer maxTokens;

    @Min(value = 1000, message = "超时不能小于1000毫秒", groups = {AddGroup.class, UpdateGroup.class})
    @Max(value = 600_000, message = "超时不能大于600000毫秒", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "请求超时毫秒")
    private Integer requestTimeoutMs;

    @Min(value = 0, message = "状态值无效", groups = {AddGroup.class, UpdateGroup.class})
    @Max(value = 1, message = "状态值无效", groups = {AddGroup.class, UpdateGroup.class})
    @Schema(description = "状态：0 正常 / 1 停用")
    private Integer status;
}
