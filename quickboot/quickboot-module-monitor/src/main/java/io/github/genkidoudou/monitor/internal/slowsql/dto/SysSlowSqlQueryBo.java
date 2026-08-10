package io.github.genkidoudou.monitor.internal.slowsql.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 慢 SQL 分页查询条件。
 */
@Data
@Schema(description = "慢 SQL 查询条件")
public class SysSlowSqlQueryBo {

    @Min(1)
    private Integer pageNum;

    @Min(1)
    @Max(500)
    private Integer pageSize;

    @Schema(description = "来源：BUSINESS / JIMU / SYSTEM")
    private String sqlSource;

    @Schema(description = "SQL 操作类型：SELECT / INSERT / UPDATE / DELETE / OTHER 等")
    private String sqlType;

    private String mapperId;

    private String sqlText;

    private String requestUri;

    private String traceId;

    private Long minCostTime;

    private String beginTime;

    private String endTime;

    private String orderByColumn;

    private String isAsc;
}
