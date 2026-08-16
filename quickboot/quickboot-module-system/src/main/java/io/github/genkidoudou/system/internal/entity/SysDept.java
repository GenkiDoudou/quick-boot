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

  /** 部门主键。 */
  @TableId(value = "dept_id", type = IdType.ASSIGN_ID)
  private Long deptId;
  /** 上级部门 ID；根节点为 0。 */
  private Long parentId;
  /** 部门名称。 */
  private String deptName;
  /** 显示顺序。 */
  private Integer orderNum;
  /** 负责人。 */
  private String leader;
  /** 联系电话。 */
  private String phone;
  /** 邮箱。 */
  private String email;
  /** 0=正常，1=停用。 */
  private String status;
}
