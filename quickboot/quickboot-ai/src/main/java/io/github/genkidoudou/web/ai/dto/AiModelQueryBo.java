package io.github.genkidoudou.web.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * AI 大模型分页查询参数。
 */
@Data
@Schema(description = "AI 大模型分页查询")
public class AiModelQueryBo {

    @Min(1)
    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Min(1)
    @Schema(description = "每页条数")
    private Integer pageSize = 10;

    @Schema(description = "名称，模糊匹配")
    private String name;

    @Schema(description = "编码，模糊匹配")
    private String code;

    @Schema(description = "模型类型：CHAT / EMBEDDING")
    private String modelType;

    @Schema(description = "厂商：OPENAI_COMPAT / OLLAMA")
    private String provider;

    @Schema(description = "状态：0 正常 / 1 停用")
    private Integer status;

    @Schema(description = "默认槽位：CHAT / EMBEDDING / WORKFLOW_CHAT")
    private String defaultSlot;
}
