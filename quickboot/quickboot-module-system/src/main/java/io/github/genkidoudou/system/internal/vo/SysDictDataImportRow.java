package io.github.genkidoudou.system.internal.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.github.genkidoudou.common.excel.annotation.ExcelDictFormat;
import lombok.Data;

@Data
@ExcelIgnoreUnannotated
public class SysDictDataImportRow {
  @ExcelProperty("排序")
  private Integer dictSort;
  @ExcelProperty("标签")
  private String dictLabel;
  @ExcelProperty("键值")
  private String dictValue;
  @ExcelProperty("字典类型")
  private String dictType;
  @ExcelProperty("样式")
  private String cssClass;
  @ExcelProperty("回显样式")
  private String listClass;
  /** 是否默认：0=否，1=是（兼容入参 Y/N）。 */
  @ExcelDictFormat(dictType = "sys_yes_no")
  @ExcelProperty("默认")
  private String isDefault;
  @ExcelDictFormat(dictType = "sys_normal_disable")
  @ExcelProperty("状态")
  private String status;
  @ExcelProperty("备注")
  private String remark;
}
