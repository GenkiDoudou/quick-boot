package io.github.genkidoudou.web.system.dict.type.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典类型返回对象。
 */
@Data
@Schema(description = "字典类型返回对象")
public class SysDictTypeVo {
    private Long dictId;
    private String dictName;
    private String dictType;
    private String status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

