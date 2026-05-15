package io.github.genkidoudou.web.system.dict.type.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import io.github.genkidoudou.common.excel.annotation.ExcelDictFormat;
import io.github.genkidoudou.common.excel.convert.ExcelDictConverter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 字典类型导入导出行模型。
 */
@Data
public class SysDictTypeExcelRow {
    @NotBlank(message = "字典名称不能为空")
    @Size(max = 100, message = "字典名称长度不能超过100")
    @ExcelProperty("字典名称")
    private String dictName;

    @NotBlank(message = "字典类型不能为空")
    @Size(max = 100, message = "字典类型长度不能超过100")
    @ExcelProperty("字典类型")
    private String dictType;

    @ExcelProperty(value = "状态", converter = ExcelDictConverter.class)
    @ExcelDictFormat(dictType = "sys_normal_disable")
    private String status;

    @Size(max = 500, message = "备注长度不能超过500")
    @ExcelProperty("备注")
    private String remark;
}
