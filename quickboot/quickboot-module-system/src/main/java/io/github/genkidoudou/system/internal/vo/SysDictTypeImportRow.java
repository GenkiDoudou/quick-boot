package io.github.genkidoudou.system.internal.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.github.genkidoudou.common.excel.annotation.ExcelDictFormat;
import lombok.Data;

/**
 * 字典类型导入行。
 */
@Data
@ExcelIgnoreUnannotated
public class SysDictTypeImportRow {

  @ExcelProperty("字典名称")
  private String dictName;

  @ExcelProperty("字典类型")
  private String dictType;

  /** 状态(sys_normal_disable) */
  @ExcelDictFormat(dictType = "sys_normal_disable")
  @ExcelProperty("状态")
  private String status;

  @ExcelProperty("备注")
  private String remark;
}
