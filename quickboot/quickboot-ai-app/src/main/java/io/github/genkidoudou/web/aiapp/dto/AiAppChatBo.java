package io.github.genkidoudou.web.aiapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 应用聊天入参。
 */
@Data
@Schema(description = "AI 应用聊天入参")
public class AiAppChatBo {

    @NotNull(message = "应用ID不能为空")
    @Schema(description = "应用ID")
    private Long appId;

    @NotNull(message = "会话ID不能为空")
    @Schema(description = "会话ID")
    private Long sessionId;

    @NotBlank(message = "消息不能为空")
    @Size(max = 8000, message = "消息长度不能超过8000")
    @Schema(description = "用户消息")
    private String message;

    @Schema(description = "是否使用草稿配置预览（编排页调试）")
    private Boolean preview;

    @Schema(description = "千问联网搜索开关")
    private Boolean webSearch;

    @Schema(description = "预览调试时传入的草稿 configJson（优先于库内 configJson，编排页未保存也可生效）")
    @Size(max = 100000, message = "预览配置过长")
    private String previewConfigJson;
}
