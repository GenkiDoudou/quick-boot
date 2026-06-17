package io.github.genkidoudou.web.aiapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 嵌入页公开应用信息（不含敏感配置）。
 */
@Data
@Schema(description = "嵌入页应用公开信息")
public class AiAppEmbedPublicVo {

    @Schema(description = "应用ID")
    private Long appId;

    @Schema(description = "应用名称")
    private String name;

    @Schema(description = "应用类型")
    private String appType;

    @Schema(description = "开场白")
    private String openingMessage;

    @Schema(description = "预设问题")
    private List<String> suggestedQuestions = new ArrayList<>();

    @Schema(description = "快捷指令")
    private List<AgentAppConfigDto.QuickCommandDto> quickCommands = new ArrayList<>();

    @Schema(description = "Chat 模型 ID（用于判断是否千问）")
    private Long chatModelId;
}
