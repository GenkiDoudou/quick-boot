package io.github.genkidoudou.common.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * Excel 导入失败明细行。
 */
@Data
public class ExcelImportErrorRow {

    @ExcelProperty("错误行号")
    private Integer rowNum;

    @ExcelProperty("错误列")
    private String columnName;

    @ExcelProperty("错误原因")
    private String reason;
}
