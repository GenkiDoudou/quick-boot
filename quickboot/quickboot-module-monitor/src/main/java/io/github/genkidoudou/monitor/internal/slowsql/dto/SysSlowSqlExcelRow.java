package io.github.genkidoudou.monitor.internal.slowsql.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 慢 SQL 导出行。
 */
@Data
public class SysSlowSqlExcelRow {

    @ExcelProperty("编号")
    @ColumnWidth(20)
    private Long slowId;

    @ExcelProperty("来源")
    @ColumnWidth(12)
    private String sqlSource;

    @ExcelProperty("操作类型")
    @ColumnWidth(12)
    private String sqlType;

    @ExcelProperty("Mapper")
    @ColumnWidth(40)
    private String mapperId;

    @ExcelProperty("耗时(ms)")
    @ColumnWidth(12)
    private Long costTime;

    @ExcelProperty("traceId")
    @ColumnWidth(24)
    private String traceId;

    @ExcelProperty("请求URI")
    @ColumnWidth(30)
    private String requestUri;

    @ExcelProperty("记录时间")
    @ColumnWidth(20)
    private LocalDateTime createTime;

    @ExcelProperty("SQL")
    @ColumnWidth(80)
    private String sqlText;
}
