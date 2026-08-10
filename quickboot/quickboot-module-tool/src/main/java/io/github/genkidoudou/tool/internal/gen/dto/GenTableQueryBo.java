package io.github.genkidoudou.tool.internal.gen.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 代码生成表分页查询条件。
 */
@Data
public class GenTableQueryBo {

    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String tableName;
    private String tableComment;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
}
