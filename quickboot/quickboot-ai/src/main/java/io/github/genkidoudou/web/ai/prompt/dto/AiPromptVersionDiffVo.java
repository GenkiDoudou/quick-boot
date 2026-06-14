package io.github.genkidoudou.web.ai.prompt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 两版本结构化 Diff VO。
 */
@Data
@Schema(description = "提示词版本 Diff")
public class AiPromptVersionDiffVo {

    @Schema(description = "左版本 ID（0 表示草稿）")
    private Long leftVersionId;

    @Schema(description = "右版本 ID（0 表示草稿）")
    private Long rightVersionId;

    @Schema(description = "内容段 Diff：sectionKey → before/after")
    private Map<String, SectionDiff> sectionDiffs;

    @Schema(description = "变量 Diff")
    private VariableDiff variableDiff;

    /**
     * 单段 before/after 差异。
     */
    @Data
    @Schema(description = "内容段差异")
    public static class SectionDiff {

        @Schema(description = "变更前")
        private String before;

        @Schema(description = "变更后")
        private String after;
    }

    /**
     * 变量列表级 Diff。
     */
    @Data
    @Schema(description = "变量差异")
    public static class VariableDiff {

        @Schema(description = "左侧变量列表")
        private List<AiPromptVariableBo> before;

        @Schema(description = "右侧变量列表")
        private List<AiPromptVariableBo> after;
    }
}
