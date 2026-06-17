package io.github.genkidoudou.web.aiapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 应用嵌入/菜单发布配置入参。
 */
@Data
@Schema(description = "AI 应用嵌入发布配置")
public class AiAppPublishVo {

    @NotNull(message = "应用ID不能为空")
    @Schema(description = "应用ID")
    private Long appId;

    @Schema(description = "嵌入令牌")
    private String embedToken;

    @Size(max = 1024, message = "域名白名单长度不能超过1024")
    @Schema(description = "域名白名单，逗号分隔")
    private String allowedOrigins;

    @Size(max = 256, message = "菜单路径长度不能超过256")
    @Schema(description = "系统菜单路由")
    private String menuPath;

    @Size(max = 256, message = "组件路径长度不能超过256")
    @Schema(description = "前端组件路径")
    private String menuComponent;

    @Schema(description = "是否启用嵌入")
    private Boolean enabled;
}
