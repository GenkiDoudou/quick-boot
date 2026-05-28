package io.github.genkidoudou.web.system.config.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统参数导出行模型。
 */
@Data
public class SysConfigExcelRow {
    @ExcelProperty("参数主键")
    private Long configId;

    @ExcelProperty("参数名称")
    private String configName;

    @ExcelProperty("参数键名")
    private String configKey;

    @ExcelProperty("参数键值")
    private String configValue;

    @ExcelProperty("系统内置")
    private String configTypeName;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
