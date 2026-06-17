package io.github.genkidoudou.web.aiapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 智能体应用 config_json 结构（{@code app_type=agent}）。
 */
@Data
@Schema(description = "智能体应用配置")
public class AgentAppConfigDto {

    @Schema(description = "Chat 模型 ID")
    private Long chatModelId;

    @Schema(description = "系统提示词 / 人设")
    private String systemPrompt;

    @Schema(description = "开场白")
    private String openingMessage;

    @Schema(description = "预设问题")
    private List<String> suggestedQuestions = new ArrayList<>();

    @Schema(description = "快捷指令")
    private List<QuickCommandDto> quickCommands = new ArrayList<>();

    @Schema(description = "绑定的知识库 ID 列表")
    private List<Long> kbIds = new ArrayList<>();

    @Schema(description = "关联工作流 Tool 绑定")
    private List<WorkflowBindingDto> workflowBindings = new ArrayList<>();

    @Schema(description = "变量记忆声明")
    private List<MemoryVariableDto> memoryVariables = new ArrayList<>();

    @Schema(description = "历史轮数截断")
    private Integer historyTurns;

    @Schema(description = "是否允许多会话")
    private Boolean multiSession;

    /**
     * 快捷指令项。
     */
    @Data
    @Schema(description = "快捷指令")
    public static class QuickCommandDto {

        private String label;
        private String prompt;
    }

    /**
     * 工作流 Tool 绑定项。
     */
    @Data
    @Schema(description = "工作流 Tool 绑定")
    public static class WorkflowBindingDto {

        private Long workflowId;
        private String toolName;
        private String description;
    }

    /**
     * 变量记忆声明项。
     */
    @Data
    @Schema(description = "变量记忆")
    public static class MemoryVariableDto {

        private String key;
        private String description;
        private String defaultValue;
    }
}
