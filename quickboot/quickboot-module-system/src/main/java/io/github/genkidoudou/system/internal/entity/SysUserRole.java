package io.github.genkidoudou.system.internal.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户-角色关联，表 {@code sys_user_role}。
 * <p>{@code userId} 对齐 {@code sys_user.user_id}（字符串）。</p>
 */
@Data
@TableName("sys_user_role")
public class SysUserRole implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /** 用户 ID，对齐 {@code sys_user.user_id}。 */
  private String userId;

  /** 角色 ID。 */
  private Long roleId;
}
