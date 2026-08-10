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
 * 字典数据 VO。
 */
@Data
@ExcelIgnoreUnannotated
public class SysDictDataVo {

  @NotNull(groups = UpdateGroup.class)
  @Null(groups = AddGroup.class)
  @ExcelProperty("字典编码")
  private Long dictCode;

  @ExcelProperty("排序")
  private Integer dictSort;

  @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
  @ExcelProperty("标签")
  private String dictLabel;

  @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
  @ExcelProperty("键值")
  private String dictValue;

  @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
  @ExcelProperty("字典类型")
  private String dictType;

  @ExcelProperty("样式")
  private String cssClass;

  @ExcelProperty("回显样式")
  private String listClass;

  /** 是否默认(sys_yes_no)：0=否，1=是。 */
  @ExcelDictFormat(dictType = "sys_yes_no")
  @ExcelProperty("默认")
  private String isDefault;

  /** 状态(sys_normal_disable)：0=正常，1=停用。 */
  @ExcelDictFormat(dictType = "sys_normal_disable")
  @ExcelProperty("状态")
  private String status;

  @ExcelProperty("备注")
  private String remark;

  /** 批量操作主键集合。 */
  private List<Long> ids;
}
