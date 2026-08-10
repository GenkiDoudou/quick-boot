package io.github.genkidoudou.system.internal.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.github.genkidoudou.common.excel.annotation.ExcelDictFormat;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

import java.util.List;

@Data
@ExcelIgnoreUnannotated
public class SysConfigVo {

  @NotNull(groups = UpdateGroup.class)
  @Null(groups = AddGroup.class)
  @ExcelProperty("参数主键")
  private Long configId;

  @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
  @ExcelProperty("参数名称")
  private String configName;

  @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
  @ExcelProperty("参数键名")
  private String configKey;

  @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
  @ExcelProperty("参数键值")
  private String configValue;

  @ExcelDictFormat(dictType = "sys_yes_no")
  @ExcelProperty("系统内置")
  private String configType;

  @ExcelProperty("备注")
  private String remark;

  private List<Long> ids;
}
