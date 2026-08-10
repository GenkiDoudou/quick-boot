package io.github.genkidoudou.system.internal.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.github.genkidoudou.common.excel.annotation.ExcelDictFormat;
import lombok.Data;

@Data
@ExcelIgnoreUnannotated
public class SysConfigImportRow {

  @ExcelProperty("参数名称")
  private String configName;

  @ExcelProperty("参数键名")
  private String configKey;

  @ExcelProperty("参数键值")
  private String configValue;

  @ExcelDictFormat(dictType = "sys_yes_no")
  @ExcelProperty("系统内置")
  private String configType;

  @ExcelProperty("备注")
  private String remark;
}
