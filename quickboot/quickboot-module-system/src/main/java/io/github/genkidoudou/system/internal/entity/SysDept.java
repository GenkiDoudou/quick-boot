package io.github.genkidoudou.system.internal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.genkidoudou.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统部门，表 {@code sys_dept}。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class SysDept extends BaseEntity {

  @TableId(value = "dept_id", type = IdType.ASSIGN_ID)
  private Long deptId;
  private Long parentId;
  private String deptName;
  private Integer orderNum;
  private String leader;
  private String phone;
  private String email;
  /** 0=正常，1=停用。 */
  private String status;
}
