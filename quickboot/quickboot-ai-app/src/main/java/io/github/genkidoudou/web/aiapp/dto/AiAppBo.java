package io.github.genkidoudou.web.aiapp.dto;

import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 应用新增/修改入参。
 */
@Data
@Schema(description = "AI 应用入参")
public class AiAppBo {

    @NotNull(message = "应用ID不能为空", groups = UpdateGroup.class)
    @Schema(description = "应用ID")
    private Long id;

    @NotBlank(message = "名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 128, message = "名称长度不能超过128")
    @Schema(description = "应用名称")
    private String name;

    @Size(max = 512, message = "描述长度不能超过512")
    @Schema(description = "功能介绍")
    private String description;

    @Size(max = 256, message = "图标长度不能超过256")
    @Schema(description = "图标")
    private String icon;

    @NotBlank(message = "应用类型不能为空", groups = AddGroup.class)
    @Schema(description = "应用类型：agent / workflow")
    private String appType;

    @Schema(description = "草稿配置 JSON 字符串")
    private String configJson;
}
