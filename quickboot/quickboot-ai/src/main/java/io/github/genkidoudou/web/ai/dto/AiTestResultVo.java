package io.github.genkidoudou.web.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI 模型连接测试结果。
 */
@Data
@Schema(description = "AI 模型连接测试结果")
public class AiTestResultVo {

    @Schema(description = "是否成功")
    private boolean success;

    @Schema(description = "探测摘要")
    private String message;

    @Schema(description = "Embedding 实测向量维度（仅 EMBEDDING 类型）")
    private Integer actualDimensions;

    @Schema(description = "Chat 探测回复摘要（仅 CHAT 类型）")
    private String replyPreview;
}
