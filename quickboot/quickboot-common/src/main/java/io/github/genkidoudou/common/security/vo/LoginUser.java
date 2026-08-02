package io.github.genkidoudou.common.security.vo;

import lombok.Data;

import java.util.Set;

/**
 * 当前登录用户（sa-token 会话解析结果）。
 */
@Data
public class LoginUser {

  /**
   * 用户id
   *
   * @since 2026/8/1
   */

  private Long userId;

  /**
   * 用户名
   *
   * @since 2026/8/1
   */

  private String username;

  /**
   * 用户昵称
   *
   * @since 2026/8/1
   */

  private String nickName;


  /**
   * 部门id
   *
   * @since 2026/8/1
   */

  private Long deptId;

  /**
   * 菜单权限
   */
  private Set<String> menuPermission;

  /**
   * 角色权限
   */
  private Set<String> rolePermission;


  /**
   * 客户端id
   *
   * @since 2026/8/1
   */

  private String clientId;
}
