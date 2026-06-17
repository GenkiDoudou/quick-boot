package io.github.genkidoudou.web.aiapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 应用消息 VO。
 */
@Data
@Schema(description = "AI 应用消息")
public class AiAppMessageVo {

    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "元数据 JSON")
    private String metadataJson;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
