package io.github.genkidoudou.web.system.config.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 系统参数查询入参。
 */
@Data
@Schema(description = "系统参数查询入参")
public class SysConfigQueryBo {
    @Schema(description = "参数名称")
    private String configName;

    @Schema(description = "参数键名")
    private String configKey;

    @Schema(description = "系统内置标记：0=否，1=是")
    private String configType;

    @Schema(description = "创建时间开始")
    private String beginTime;

    @Schema(description = "创建时间结束")
    private String endTime;
}
