package io.github.genkidoudou.common.security.vo;

import lombok.Data;

import java.util.Set;

/**
 * 当前登录用户（sa-token 会话解析结果）。
 */
@Data
public class LoginUser {

  /** 用户主键。 */
  private Long userId;

  /** 登录账号。 */
  private String username;

  /** 用户昵称。 */
  private String nickName;

  /** 所属部门 id。 */
  private Long deptId;

  /** 菜单权限标识集合。 */
  private Set<String> menuPermission;

  /** 角色权限标识集合。 */
  private Set<String> rolePermission;

  /** OAuth 客户端 id（多端登录场景）。 */
  private String clientId;
}
