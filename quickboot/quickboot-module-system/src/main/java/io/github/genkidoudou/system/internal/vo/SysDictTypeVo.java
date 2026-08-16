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
 * 字典类型 VO。
 */
@Data
@ExcelIgnoreUnannotated
public class SysDictTypeVo {

  /** 字典类型主键。 */
  @NotNull(groups = UpdateGroup.class)
  @Null(groups = AddGroup.class)
  @ExcelDictFormat(dictType = "sys_normal_disable")
  @ExcelProperty("字典ID")
  private Long dictId;

  /** 字典名称。 */
  @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
  @ExcelProperty("字典名称")
  private String dictName;

  /** 字典类型编码，唯一。 */
  @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
  @ExcelProperty("字典类型")
  private String dictType;

  /** 状态：0=正常，1=停用。 */
  @ExcelProperty("状态")
  private String status;

  /** 备注。 */
  @ExcelProperty("备注")
  private String remark;

  /** 批量操作主键集合。 */
  private List<Long> ids;
}
