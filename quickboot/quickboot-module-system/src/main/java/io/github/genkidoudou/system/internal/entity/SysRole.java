package io.github.genkidoudou.system.internal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.genkidoudou.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统角色，表 {@code sys_role}。
 * <p>status：{@code 0}=正常，{@code 1}=停用；del_flag 见基类。</p>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_role")
public class SysRole extends BaseEntity implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /** 角色主键。 */
  @TableId(value = "role_id", type = IdType.ASSIGN_ID)
  private Long roleId;

  /** 角色名称。 */
  private String roleName;

  /** 权限字符，唯一 */
  private String roleKey;

  /** 显示顺序，越小越靠前。 */
  private Integer roleSort;

  /**
   * 数据范围（本期仅占位，默认 1）
   */
  private String dataScope;

  /**
   * 状态：0=正常，1=停用
   */
  private String status;
}
