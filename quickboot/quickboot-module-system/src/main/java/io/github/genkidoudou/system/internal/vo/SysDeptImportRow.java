package io.github.genkidoudou.system.internal.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.github.genkidoudou.common.excel.annotation.ExcelDictFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 部门导入行。
 */
@Data
@ExcelIgnoreUnannotated
public class SysDeptImportRow {
  @ExcelProperty("上级部门ID")
  private Long parentId;
  @NotBlank(message = "部门名称不能为空")
  @ExcelProperty("部门名称")
  private String deptName;
  @ExcelProperty("显示顺序")
  private Integer orderNum;
  @ExcelProperty("负责人")
  private String leader;
  @ExcelProperty("联系电话")
  private String phone;
  @ExcelProperty("邮箱")
  private String email;
  @ExcelDictFormat(dictType = "sys_normal_disable")
  @ExcelProperty("状态")
  private String status;
  @ExcelProperty("备注")
  private String remark;
}
