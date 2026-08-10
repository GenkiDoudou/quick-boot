package io.github.genkidoudou.tool.internal.gen.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 代码生成全局默认配置（来自参数设置 qc.gen.*）。
 */
@Data
@Schema(description = "代码生成默认配置")
public class GenDefaultsVo {

    @Schema(description = "生成包路径")
    private String packageName;

    @Schema(description = "生成模块名")
    private String moduleName;

    @Schema(description = "作者")
    private String functionAuthor;

    @Schema(description = "模板类型")
    private String tplCategory;

    @Schema(description = "上级菜单 ID")
    private Long parentMenuId;
}
