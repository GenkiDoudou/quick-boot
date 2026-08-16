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
 * 部门读写及导出对象。
 */
@Data
@ExcelIgnoreUnannotated
public class SysDeptVo {
  /** 部门主键。 */
  @NotNull(groups = UpdateGroup.class, message = "部门ID不能为空")
  @Null(groups = AddGroup.class, message = "新增时部门ID必须为空")
  @ExcelProperty("部门ID")
  private Long deptId;
  /** 上级部门 ID。 */
  @ExcelProperty("上级部门ID")
  private Long parentId;
  /** 部门名称。 */
  @NotBlank(groups = {AddGroup.class, UpdateGroup.class}, message = "部门名称不能为空")
  @ExcelProperty("部门名称")
  private String deptName;
  /** 显示顺序。 */
  @ExcelProperty("显示顺序")
  private Integer orderNum;
  /** 负责人。 */
  @ExcelProperty("负责人")
  private String leader;
  /** 联系电话。 */
  @ExcelProperty("联系电话")
  private String phone;
  /** 邮箱。 */
  @ExcelProperty("邮箱")
  private String email;
  /** 状态(sys_normal_disable)。 */
  @ExcelDictFormat(dictType = "sys_normal_disable")
  @ExcelProperty("状态")
  private String status;
  /** 备注。 */
  @ExcelProperty("备注")
  private String remark;
  /** 批量操作主键集合。 */
  private List<Long> ids;
  /** 子部门（树形展示）。 */
  private List<SysDeptVo> children;
}
