package io.github.genkidoudou.system.internal.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.github.genkidoudou.common.excel.annotation.ExcelDictFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色导入行。
 */
@ExcelIgnoreUnannotated
@Data
public class SysRoleImportRow implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;
  @NotBlank(message = "角色名称不能为空")
  @ExcelProperty(value = "角色名称", index = 0)
  private String roleName;

  @NotBlank(message = "权限字符不能为空")
  @ExcelProperty(value = "权限字符", index = 1)
  private String roleKey;

  @ExcelProperty(value = "显示顺序", index = 2)
  private Integer roleSort;

  @ExcelDictFormat(dictType = "sys_role_data_scope")
  @ExcelProperty(value = "数据范围", index = 3)
  private String dataScope;

  @ExcelDictFormat(dictType = "sys_normal_disable")
  @ExcelProperty(value = "状态", index = 4)
  private String status;

  @ExcelProperty(value = "备注", index = 5)
  private String remark;
}
