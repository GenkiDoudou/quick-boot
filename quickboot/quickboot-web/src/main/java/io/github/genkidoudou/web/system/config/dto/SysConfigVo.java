package io.github.genkidoudou.web.system.config.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统参数返回对象。
 */
@Data
@Schema(description = "系统参数返回对象")
public class SysConfigVo {
    private Long configId;
    private String configName;
    private String configKey;
    private String configValue;
    private String configType;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
