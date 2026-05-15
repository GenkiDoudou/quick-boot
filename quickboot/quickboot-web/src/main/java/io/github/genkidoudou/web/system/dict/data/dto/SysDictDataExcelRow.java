package io.github.genkidoudou.web.system.dict.data.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import io.github.genkidoudou.common.excel.annotation.ExcelDictFormat;
import io.github.genkidoudou.common.excel.convert.ExcelDictConverter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 字典数据导入导出行模型。
 */
@Data
public class SysDictDataExcelRow {
    @ExcelProperty("字典类型")
    private String dictType;

    @NotBlank(message = "数据标签不能为空")
    @Size(max = 100, message = "数据标签长度不能超过100")
    @ExcelProperty("数据标签")
    private String dictLabel;

    @NotBlank(message = "数据键值不能为空")
    @Size(max = 100, message = "数据键值长度不能超过100")
    @ExcelProperty("数据键值")
    private String dictValue;

    @ExcelProperty("排序")
    private Integer dictSort;

    @ExcelProperty(value = "状态", converter = ExcelDictConverter.class)
    @ExcelDictFormat(dictType = "sys_normal_disable")
    private String status;

    @Size(max = 100, message = "样式长度不能超过100")
    @ExcelProperty("样式")
    private String cssClass;

    @Size(max = 100, message = "回显样式长度不能超过100")
    @ExcelProperty("回显样式")
    private String listClass;

    @Size(max = 500, message = "备注长度不能超过500")
    @ExcelProperty("备注")
    private String remark;
}
