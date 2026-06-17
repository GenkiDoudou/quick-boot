package io.github.genkidoudou.web.aiapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 应用列表项 VO。
 */
@Data
@Schema(description = "AI 应用列表项")
public class AiAppVo {

    @Schema(description = "应用ID")
    private Long id;

    @Schema(description = "应用名称")
    private String name;

    @Schema(description = "功能介绍")
    private String description;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "应用类型")
    private String appType;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
