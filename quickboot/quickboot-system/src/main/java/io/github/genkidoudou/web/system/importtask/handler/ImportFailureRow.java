package io.github.genkidoudou.web.system.importtask.handler;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 导入失败明细 Excel 行（仅行号+原因，已由 {@link io.github.genkidoudou.common.excel.ExcelFailureExport} 替代）。
 *
 * @deprecated 编排器使用带原始列的失败明细导出
 */
@Deprecated
@Data
public class ImportFailureRow {

    @ExcelProperty("错误行号")
    private Integer rowNo;

    @ExcelProperty("错误原因")
    private String errorMsg;
}
