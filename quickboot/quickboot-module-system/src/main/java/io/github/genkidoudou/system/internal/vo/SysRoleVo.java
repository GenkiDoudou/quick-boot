package io.github.genkidoudou.system.internal.vo;

import cn.hutool.core.date.DatePattern;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.github.genkidoudou.common.excel.annotation.ExcelDictFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色 VO。
 * <p>status：{@code 0}=正常，{@code 1}=停用。</p>
 */
@ExcelIgnoreUnannotated
@Data
public class SysRoleVo implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * 主键
   */
  @NotNull(message = "角色ID不能为空", groups = UpdateGroup.class)
  @Null(message = "新增时角色ID必须为空", groups = AddGroup.class)
  @ExcelProperty(value = "角色ID", index = 0)
  private Long roleId;

  @NotBlank(message = "角色名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
  @ExcelProperty(value = "角色名称", index = 1)
  private String roleName;

  @NotBlank(message = "权限字符不能为空", groups = {AddGroup.class, UpdateGroup.class})
  @ExcelProperty(value = "权限字符", index = 2)
  private String roleKey;

  @ExcelProperty(value = "显示顺序", index = 3)
  private Integer roleSort;

  @ExcelDictFormat(dictType = "sys_role_data_scope")
  @ExcelProperty(value = "数据范围", index = 4)
  private String dataScope;

  /**
   * 0=正常 1=停用
   */
  @NotBlank(message = "状态不能为空", groups = AddGroup.class)
  @ExcelDictFormat(dictType = "sys_normal_disable")
  @ExcelProperty(value = "状态", index = 5)
  private String status;

  @ExcelProperty(value = "备注", index = 6)
  private String remark;

  @ExcelProperty(value = "创建时间", index = 7)
  @com.alibaba.excel.annotation.format.DateTimeFormat(value = DatePattern.NORM_DATETIME_MINUTE_PATTERN)
  @JsonFormat(pattern = DatePattern.NORM_DATETIME_MINUTE_PATTERN)
  @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_MINUTE_PATTERN)
  private LocalDateTime createTime;

  /**
   * 勾选主键；非空时忽略搜索条件
   */
  private List<Long> ids;
}
