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

    /** 页码，最小 1 */
    @Min(1)
    private Integer pageNum;

    /** 每页条数，1~500 */
    @Min(1)
    @Max(500)
    private Integer pageSize;

    @Schema(description = "来源：BUSINESS / JIMU / SYSTEM")
    private String sqlSource;

    @Schema(description = "SQL 操作类型：SELECT / INSERT / UPDATE / DELETE / OTHER 等")
    private String sqlType;

    /** Mapper 标识，模糊匹配 */
    private String mapperId;

    /** SQL 文本，模糊匹配 */
    private String sqlText;

    /** 触发慢 SQL 的请求 URI，模糊匹配 */
    private String requestUri;

    /** 链路标识，精确匹配 */
    private String traceId;

    /** 最小耗时毫秒 */
    private Long minCostTime;

    /** 开始日期 yyyy-MM-dd */
    private String beginTime;

    /** 结束日期 yyyy-MM-dd */
    private String endTime;

    /** 排序列，支持 costTime */
    private String orderByColumn;

    /** 排序方向：asc / desc */
    private String isAsc;
}
