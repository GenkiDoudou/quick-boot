package io.github.genkidoudou.web.aiapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 应用会话 VO。
 */
@Data
@Schema(description = "AI 应用会话")
public class AiAppSessionVo {

    @Schema(description = "会话ID")
    private Long id;

    @Schema(description = "应用ID")
    private Long appId;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "变量记忆 JSON")
    private String variablesJson;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
