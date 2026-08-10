package io.github.genkidoudou.system.internal.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.github.genkidoudou.common.desensitization.Sensitive;
import io.github.genkidoudou.common.desensitization.SensitiveType;
import io.github.genkidoudou.common.excel.annotation.ExcelDictFormat;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

import java.util.List;

@Data
@ExcelIgnoreUnannotated
public class SysUserVo {
  @NotNull(groups = UpdateGroup.class)
  @Null(groups = AddGroup.class)
  @ExcelProperty("用户ID")
  private Long userId;

  @ExcelProperty("部门ID")
  private Long deptId;

  @ExcelProperty("部门名称")
  private String deptName;

  @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
  @ExcelProperty("用户账号")
  private String userName;

  @ExcelProperty("用户昵称")
  private String nickName;

  @ExcelProperty("邮箱")
  private String email;

  @ExcelProperty("手机号")
  @Sensitive(type = SensitiveType.MOBILE)
  private String phonenumber;

  /** 性别(sys_user_sex) */
  @ExcelDictFormat(dictType = "sys_user_sex")
  @ExcelProperty("性别")
  private String sex;

  /** 仅新增可写；列表/详情/导出不返回 */
  private String password;

  /** 状态(sys_normal_disable) */
  @ExcelDictFormat(dictType = "sys_normal_disable")
  @ExcelProperty("状态")
  private String status;

  @ExcelProperty("备注")
  private String remark;

  @NotEmpty(groups = {AddGroup.class, UpdateGroup.class}, message = "请选择角色")
  private List<Long> roleIds;

  private String roleNames;

  private List<Long> ids;
}
