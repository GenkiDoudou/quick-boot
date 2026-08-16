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

/**
 * 系统参数配置读写、导入导出 VO。
 */
@Data
@ExcelIgnoreUnannotated
public class SysConfigVo {

  /** 参数主键。 */
  @NotNull(groups = UpdateGroup.class)
  @Null(groups = AddGroup.class)
  @ExcelProperty("参数主键")
  private Long configId;

  /** 参数名称。 */
  @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
  @ExcelProperty("参数名称")
  private String configName;

  /** 参数键名。 */
  @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
  @ExcelProperty("参数键名")
  private String configKey;

  /** 参数键值。 */
  @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
  @ExcelProperty("参数键值")
  private String configValue;

  /** 系统内置(sys_yes_no)：0=否，1=是。 */
  @ExcelDictFormat(dictType = "sys_yes_no")
  @ExcelProperty("系统内置")
  private String configType;

  /** 备注。 */
  @ExcelProperty("备注")
  private String remark;

  /** 批量操作主键集合。 */
  private List<Long> ids;
}
