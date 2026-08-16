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

  /** 字典数据主键。 */
  @NotNull(groups = UpdateGroup.class)
  @Null(groups = AddGroup.class)
  @ExcelProperty("字典编码")
  private Long dictCode;

  /** 显示排序。 */
  @ExcelProperty("排序")
  private Integer dictSort;

  /** 字典标签（展示文本）。 */
  @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
  @ExcelProperty("标签")
  private String dictLabel;

  /** 字典键值（存库值）。 */
  @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
  @ExcelProperty("键值")
  private String dictValue;

  /** 所属字典类型编码。 */
  @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
  @ExcelProperty("字典类型")
  private String dictType;

  /** CSS 样式类。 */
  @ExcelProperty("样式")
  private String cssClass;

  /** 回显样式（如 tag 颜色）。 */
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

  /** 备注。 */
  @ExcelProperty("备注")
  private String remark;

  /** 批量操作主键集合。 */
  private List<Long> ids;
}
