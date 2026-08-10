package io.github.genkidoudou.system.internal.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.github.genkidoudou.common.excel.annotation.ExcelDictFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户导入行（样例：DictFormat + Validation 驱动模板约束）。
 */
@Data
@ExcelIgnoreUnannotated
public class SysUserImportRow {
  @NotBlank(message = "用户账号不能为空")
  @ExcelProperty("用户账号")
  private String userName;

  @NotBlank(message = "用户昵称不能为空")
  @ExcelProperty("用户昵称")
  private String nickName;

  @ExcelProperty("部门名称")
  private String deptName;

  @Email(message = "邮箱格式不正确")
  @ExcelProperty("邮箱")
  private String email;

  @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
  @ExcelProperty("手机号")
  private String phonenumber;

  /** 性别(sys_user_sex) */
  @ExcelDictFormat(dictType = "sys_user_sex")
  @ExcelProperty("性别")
  private String sex;

  /** 状态(sys_normal_disable) */
  @ExcelDictFormat(dictType = "sys_normal_disable")
  @ExcelProperty("状态")
  private String status;

  @ExcelProperty("备注")
  private String remark;
}
