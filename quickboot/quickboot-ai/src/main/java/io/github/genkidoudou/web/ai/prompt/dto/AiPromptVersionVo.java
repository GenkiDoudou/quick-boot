package io.github.genkidoudou.web.ai.prompt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 提示词版本列表/详情 VO。
 */
@Data
@Schema(description = "提示词版本视图对象")
public class AiPromptVersionVo {

    @Schema(description = "版本 ID")
    private Long versionId;

    @Schema(description = "提示词 ID")
    private Long promptId;

    @Schema(description = "版本号")
    private Integer versionNo;

    @Schema(description = "变更摘要")
    private String changeSummary;

    @Schema(description = "版本来源")
    private String source;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "内容段快照")
    private Map<String, String> sections;

    @Schema(description = "变量声明快照")
    private List<AiPromptVariableBo> variables;
}
