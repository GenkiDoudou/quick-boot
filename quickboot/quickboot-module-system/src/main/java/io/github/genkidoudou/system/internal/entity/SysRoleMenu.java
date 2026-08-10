package io.github.genkidoudou.system.internal.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色-菜单关联，表 {@code sys_role_menu}。
 */
@Data
@TableName("sys_role_menu")
public class SysRoleMenu implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private Long roleId;

  private Long menuId;
}
