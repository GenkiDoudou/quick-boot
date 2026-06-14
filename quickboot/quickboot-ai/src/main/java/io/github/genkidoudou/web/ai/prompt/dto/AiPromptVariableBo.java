package io.github.genkidoudou.web.ai.prompt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 提示词变量声明入参。
 */
@Data
@Schema(description = "提示词变量声明")
public class AiPromptVariableBo {

    @NotBlank(message = "变量键名不能为空")
    @Size(max = 64, message = "变量键名长度不能超过64")
    @Schema(description = "变量键名")
    private String varKey;

    @Schema(description = "变量类型：string / number / array / object")
    private String varType;

    @Schema(description = "是否必填：0 否 / 1 是")
    private Integer required;

    @Size(max = 200, message = "变量说明长度不能超过200")
    @Schema(description = "变量说明")
    private String description;

    @Schema(description = "排序号")
    private Integer sort;
}
