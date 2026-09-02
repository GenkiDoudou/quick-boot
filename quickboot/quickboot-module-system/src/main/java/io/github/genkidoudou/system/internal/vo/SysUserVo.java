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

/**
 * 系统用户读写、导入导出及批量操作 VO。
 */
@Data
@ExcelIgnoreUnannotated
public class SysUserVo {
  /** 用户主键。 */
  @NotNull(groups = UpdateGroup.class)
  @Null(groups = AddGroup.class)
  @ExcelProperty("用户ID")
  private Long userId;

  /** 所属部门 ID。 */
  @ExcelProperty("部门ID")
  private Long deptId;

  /** 部门名称（展示用，非持久化）。 */
  @ExcelProperty("部门名称")
  private String deptName;

  /** 登录账号。 */
  @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
  @ExcelProperty("用户账号")
  private String userName;

  /** 用户昵称。 */
  @ExcelProperty("用户昵称")
  private String nickName;

  /** 邮箱；列表分页 JSON 响应经 {@link Sensitive} 脱敏，详情/编辑接口原样返回便于回显。 */
  @ExcelProperty("邮箱")
  @Sensitive(type = SensitiveType.EMAIL)
  private String email;

  /** 手机号。 */
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

  /** 备注。 */
  @ExcelProperty("备注")
  private String remark;

  /** 关联角色 ID 列表。 */
  @NotEmpty(groups = {AddGroup.class, UpdateGroup.class}, message = "请选择角色")
  private List<Long> roleIds;

  /** 角色名称汇总（展示用）。 */
  private String roleNames;

  /** 批量操作主键集合。 */
  private List<Long> ids;
}
